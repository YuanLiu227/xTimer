# xTimer 定时微服务项目说明

## 项目定位

`xTimer` 是一个通用定时微服务。业务系统可以把需要定时执行的任务注册到本服务中，由本服务负责解析 Cron 表达式、生成具体执行任务、按时间触发任务，并在触发时间到达后通过 HTTP 回调通知业务方。

这个项目的定位类似“分布式闹钟”：

- 本服务负责保存定时规则、计算执行时间、触发回调、记录执行结果。
- 业务系统负责提供回调接口，并保证自己的业务处理逻辑具备幂等能力。
- 本服务不处理业务本身，只负责在合适的时间提醒业务系统执行。

## 核心目标

项目围绕三个核心问题设计：

1. 高精度

   定时任务的执行误差应尽可能小。这里的误差指：

   ```text
   误差时间 = 任务实际触发时间 - 任务预期执行时间
   ```

   当前项目通过 Redis ZSet、秒级调度器、触发器轮询和线程池执行来降低误差。

2. 高负载

   同一时间可能存在大量任务需要执行。如果所有任务串行处理，后面的任务会产生较大延迟。项目通过二维分片、线程池和 Redis 缓存降低集中执行压力。

3. 异常处理

   Redis、MySQL、应用进程、业务回调接口都可能异常。项目通过 MySQL 持久化、Redis 缓存、分布式锁、重复扫描上一分钟分片、任务状态回写等方式提高容错能力。

## 技术栈

- Java 17
- Spring Boot
- Spring Scheduling
- Spring Async
- MyBatis
- MySQL
- Redis
- Quartz CronExpression
- RestTemplate

## 项目模块

主要代码目录如下：

```text
src/main/java/com/yuanliu/xtimer
├── common       通用模型、响应体、配置、线程池、Redis 基础能力
├── controller   对外 HTTP 接口
├── dto          请求与回调参数 DTO
├── enums        Timer 和 Task 状态枚举
├── exception    业务异常与错误码
├── manager      迁移器核心逻辑
├── mapper       MyBatis Mapper 接口
├── model        数据库模型
├── redis        Redis 配置、分布式锁、任务缓存
├── service      定时器服务、迁移器、调度器、触发器、执行器
└── utils        时间、Cron、JSON 工具
```

核心模块职责：

| 模块 | 主要类 | 职责 |
| --- | --- | --- |
| 定时器服务 | `XTimerServiceImpl` | 创建 Timer、激活 Timer，并在激活时触发任务迁移 |
| 迁移器 | `MigratorWorker`, `MigrateManagerImpl` | 扫描已激活 Timer，解析 Cron，生成具体任务 |
| 调度器 | `SchedulerWorker`, `SchedulerTask` | 每秒扫描当前分钟和上一分钟的二维分片 |
| 触发器 | `TriggerWorker`, `TriggerTimerTask`, `TriggerPoolTask` | 在分钟分片内按秒取出到期任务并提交执行器 |
| 执行器 | `ExecutorWorker` | 抢占任务、执行 HTTP 回调、回写执行结果 |
| Redis 缓存 | `TaskCache` | 将具体任务写入 Redis ZSet，并从 ZSet 中按时间范围查询 |
| 分布式锁 | `DistributeLock` | 使用 Redis setIfAbsent 和 Lua 脚本实现基础锁与续期 |

## 数据库设计

项目使用两个核心表。

### xtimer

`xtimer` 保存用户创建的定时器，也就是用户级任务。

主要字段：

| 字段 | 含义 |
| --- | --- |
| `timer_id` | 定时器主键 |
| `app` | 业务应用标识 |
| `name` | 定时器名称 |
| `status` | 定时器状态，`1` 表示未激活，`2` 表示激活 |
| `cron` | Cron 表达式 |
| `notify_http_param` | HTTP 回调参数 JSON |
| `create_time` | 创建时间 |
| `modify_time` | 修改时间 |

### timer_task

`timer_task` 保存由 `xtimer` 迁移出来的具体执行任务。

主要字段：

| 字段 | 含义 |
| --- | --- |
| `task_id` | 具体任务主键 |
| `timer_id` | 所属定时器 ID |
| `app` | 业务应用标识 |
| `output` | 执行输出或失败原因 |
| `status` | 任务状态 |
| `run_timer` | 预期执行时间戳，毫秒 |
| `cost_time` | 当前实现中表示触发误差，单位毫秒 |
| `create_time` | 创建时间 |
| `modify_time` | 修改时间 |

`timer_task` 有唯一索引：

```sql
unique key idx_timer_id_run_timer (timer_id, run_timer)
```

该索引用于保证同一个定时器在同一个执行时间点不会重复生成具体任务。

## 状态说明

### TimerStatus

```text
Unable = 1
Enable = 2
```

`xtimer.status = 2` 时，迁移器才会为该定时器生成具体任务，执行器也只会回调激活状态的定时器。

### TaskStatus

```text
NotRun  = 0
Running = 1
Succeed = 2
Failed  = 3
```

任务正常状态流转：

```text
NotRun -> Running -> Succeed
NotRun -> Running -> Failed
```

执行器通过下面的 CAS 风格 SQL 抢占任务：

```sql
update timer_task
set status = Running
where task_id = ?
  and status = NotRun;
```

只有影响行数为 `1` 的执行线程才可以继续执行回调。这样可以避免同一个任务被多个执行器线程重复执行。

## Redis 设计

项目使用 Redis ZSet 缓存近期要执行的具体任务。

### 任务分片 Key

任务缓存 key 格式：

```text
yyyy-MM-dd HH:mm_bucketId
```

示例：

```text
2026-05-15 09:30_1
```

### ZSet Score

Score 为任务预期执行时间戳：

```text
run_timer
```

单位为毫秒。

### ZSet Member

Member 为：

```text
timerId_runTimer
```

示例：

```text
1_1778812200000
```

执行器拿到这个 member 后，会拆分出 `timerId` 和 `runTimer`，再到 MySQL 中定位唯一的 `timer_task`。

### 分片规则

当前分片 bucket 计算方式：

```java
bucketId = timerId % bucketsNum
```

默认 bucket 数量在 `SchedulerAppConf` 中配置为 `5`。

## 核心流程

### 1. 创建 Timer

业务方调用：

```text
POST /xtimer/createTimer
```

服务会校验 Cron 表达式，然后将定时器保存到 `xtimer` 表。

创建 Timer 只负责保存用户级定时任务，不一定立即生成 `timer_task`。实际使用时建议先创建未激活状态的 Timer，再调用激活接口。

### 2. 激活 Timer

业务方调用：

```text
GET /xtimer/enableTimer?app={app}&timerId={timerId}
```

激活逻辑：

1. 使用 Redis 分布式锁限制同一应用的频繁激活操作。
2. 查询 `xtimer`。
3. 将 Timer 状态更新为 `Enable`。
4. 调用迁移器立即生成未来一段时间内的具体任务。

### 3. 迁移具体任务

迁移器由两个入口触发：

1. Timer 激活时立即迁移。
2. `MigratorWorker` 定时迁移。

迁移逻辑：

1. 校验 Timer 必须处于 `Enable` 状态。
2. 使用 Quartz `CronExpression` 解析 Cron 表达式。
3. 生成未来一段时间内的执行时间点。
4. 批量写入 `timer_task`。
5. 批量写入 Redis ZSet。

当前代码中迁移窗口由：

```java
TimerUtils.GetForwardTwoMigrateStepEnd(now, migratorAppConf.getMigrateStepMinutes())
```

决定。默认 `migrateStepMinutes = 60`，因此会生成从当前时间开始往后约 120 分钟内的任务。

### 4. 定时迁移

`MigratorWorker` 使用 `@Scheduled(fixedRate = 10 * 1000)` 每 10 秒运行一次。

为了避免多个实例重复迁移，迁移前会抢占 Redis 锁：

```text
migrator_lock_yyyy-MM-dd HH
```

当前锁过期时间使用：

```java
60L * migratorAppConf.getMigrateTryLockMinutes()
```

默认 `migrateTryLockMinutes = 1`，即 60 秒。

### 5. 调度器扫描分片

`SchedulerWorker` 每 1 秒执行一次。

每次会扫描：

1. 当前分钟的所有 bucket。
2. 上一分钟的所有 bucket。

扫描上一分钟是兜底机制：如果上一分钟因为进程抖动、锁超时、触发器中断等原因没有完整执行，下一分钟还有机会重新扫描。

每个分片由 `SchedulerTask` 处理，处理前会抢占分片锁：

```text
time_bucket_lock_yyyy-MM-dd HH:mm_bucketId
```

抢锁成功后，调度器会把分片 key 交给触发器。

### 6. 触发器按秒取任务

`TriggerWorker` 接收到分钟级分片 key 后，会创建 `TriggerTimerTask`。

`TriggerTimerTask` 在这一分钟内按秒扫描：

```text
[tStart, tStart + zrangeGapSeconds)
```

默认 `zrangeGapSeconds = 1`，因此触发器会以 1 秒为单位取出到期任务。

取任务优先级：

1. 先查 Redis ZSet。
2. Redis 查到任务则直接返回。
3. Redis 抛异常或没有查到任务时，回查 MySQL 兜底。

这保证了 Redis 缓存丢失时，仍然可以从 `timer_task` 中找回未执行任务。

### 7. 执行器执行回调

`TriggerPoolTask` 将任务提交给线程池，最终调用 `ExecutorWorker`。

执行器逻辑：

1. 解析 `timerId_runTimer`。
2. 查询对应 `timer_task`。
3. 使用 CAS 风格 SQL 将任务从 `NotRun` 抢占为 `Running`。
4. 查询对应 `xtimer`。
5. 如果 Timer 已取消激活，则任务写为 `Failed`，输出 `timer disabled`。
6. 如果 Timer 仍激活，则执行 HTTP 回调。
7. 回调 2xx 成功，任务写为 `Succeed`。
8. 回调异常、无响应或非 2xx，任务写为 `Failed`。

## HTTP 回调

Timer 的回调配置保存在 `notify_http_param` 字段中。

示例：

```json
{
  "method": "POST",
  "url": "http://127.0.0.1:8082/xtimer/callback",
  "body": "its time on. this is a callback msg"
}
```

当前执行器主要支持 `POST`：

```java
restTemplate.postForEntity(httpParam.getUrl(), httpParam.getBody(), String.class);
```

业务方需要提供对应的 HTTP 接口。若接口不存在或返回非 2xx，任务会被标记为失败。

## 对外接口

当前已实现接口：

### 创建 Timer

```text
POST /xtimer/createTimer
```

请求体示例：

```json
{
  "app": "testXtimer",
  "name": "测试Xtimer",
  "status": 1,
  "cron": "*/5 * * ? * *",
  "notifyHTTPParam": {
    "method": "POST",
    "url": "http://127.0.0.1:8082/xtimer/callback",
    "body": "its time on. this is a callback msg"
  }
}
```

返回：

```json
{
  "code": 0,
  "msg": "ok",
  "data": 1
}
```

### 激活 Timer

```text
GET /xtimer/enableTimer?app=testXtimer&timerId=1
```

返回：

```json
{
  "code": 0,
  "msg": "ok",
  "data": "ok"
}
```

## 配置说明

数据库和 Redis 配置位于：

```text
src/main/resources/application.yml
```

主要配置：

```yaml
server:
  port: 8082

spring:
  datasource:
    url: jdbc:mysql://127.0.0.1:3306/xtimer
    username: root
    password: 1234
  redis:
    host: 127.0.0.1
    port: 6379
```

项目配置类：

| 配置类 | 说明 |
| --- | --- |
| `SchedulerAppConf` | bucket 数量、调度锁过期时间、调度线程池配置 |
| `TriggerAppConf` | 触发器扫描间隔、触发线程池配置 |
| `MigratorAppConf` | 迁移步长、迁移锁时间 |

## 精度说明

当前项目的执行误差主要由三部分组成：

```text
误差时间 =
调度器每 1 秒扫描 Redis 分片的等待时间
+ 触发器每 1 秒扫描分片内任务的等待时间
+ 执行器线程调度和真正执行回调前的等待时间
```

当前 `timer_task.cost_time` 实际记录的是：

```text
任务实际触发时间 - 任务预期执行时间
```

因此它更接近“调度延迟”或“触发误差”，不是 HTTP 回调接口自身耗时。

## 幂等与 At Least Once

调度器和触发器只能保证任务至少被触发一次，即 At Least Once。

原因是：

- 触发器取出任务和延长分布式锁不是原子操作。
- 应用可能在触发任务后、更新锁前宕机。
- 调度器会重复扫描上一分钟分片作为兜底。

因此同一个任务可能被重复提交给执行器。最终的去重由执行器完成：

```text
timer_task.status = NotRun
```

只有状态为 `NotRun` 的任务可以被抢占为 `Running`。抢占成功的线程继续执行，抢占失败的线程直接返回。

## 异常处理设计

当前项目已经具备基础异常处理：

- Timer 创建时校验 Cron 表达式。
- Redis 分片丢失时，触发器回查 MySQL。
- HTTP 回调异常时，任务写为 `Failed`。
- HTTP 回调 2xx 时，任务写为 `Succeed`。
- Timer 取消激活后，已触发的具体任务会写为 `Failed`，输出 `timer disabled`。

设计文档中还提到一个尚未完全实现的兜底脚本：

```text
每 5 分钟扫描 timer_task，将异常任务检索出来并执行。
```

建议后续实现为：

1. 扫描长时间停留在 `Running` 的任务。
2. 将其原子改回 `NotRun`。
3. 再调用执行器重新抢占执行。
4. 增加 `retry_count` 字段，避免永久失败任务被无限重试。
5. 对超过重试次数或长时间失败的任务触发报警。

## 项目当前已实现能力

当前项目已经实现了定时微服务的最小可用闭环：

- 创建用户级 Timer。
- 激活 Timer。
- 解析 Cron 表达式。
- 批量生成未来一段时间内的具体任务。
- 持久化任务到 MySQL。
- 缓存任务到 Redis ZSet。
- 按分钟和 bucket 进行二维分片。
- 调度器每秒扫描当前分钟和上一分钟分片。
- 触发器在分钟内按秒取任务。
- 执行器通过 HTTP 回调业务方。
- 执行结果回写 `timer_task`。
- 使用任务状态 CAS 防止多线程重复执行。

## 后续可完善点

为了从最小可用版本走向更完整的生产版本，可以继续补充：

1. Timer 管理接口

   - 删除 Timer
   - 取消激活 Timer
   - 查询 Timer
   - 更新 Timer

2. 异常任务补偿器

   - 每 5 分钟扫描异常任务
   - 重试超时 Running 任务
   - 限制最大重试次数
   - 触发告警

3. HTTP 回调参数校验

   - 校验 `notifyHTTPParam` 不为空
   - 校验 URL 不为空
   - 校验 method 是否支持
   - 支持更多 HTTP 方法

4. 更完整的分布式锁

   - 支持可重入锁
   - 支持更明确的释放逻辑
   - 支持锁续期失败监控

5. 管理与观测

   - 查询任务执行历史
   - 查询失败任务
   - 统计执行延迟
   - 统计成功率和失败率
   - 接入日志和监控告警

6. 微服务生态接入

   - Nacos 注册发现
   - Gateway 路由
   - Feign 客户端
   - 多实例部署验证

## 运行准备

1. 创建 MySQL 数据库：

```sql
create database xtimer default character set utf8mb4 collate utf8mb4_general_ci;
```

2. 执行建表脚本：

```text
db/xTimer.sql
```

3. 启动 Redis：

```text
127.0.0.1:6379
```

4. 确认 `application.yml` 中 MySQL 和 Redis 配置正确。

5. 启动 Spring Boot 应用。

## 推荐使用流程

1. 创建未激活 Timer。
2. 调用激活接口。
3. 检查 `timer_task` 是否生成具体任务。
4. 检查 Redis 是否生成分片 ZSet。
5. 等待调度器和触发器触发任务。
6. 检查 `timer_task.status` 和 `output`。

推荐创建 Timer 时使用：

```json
{
  "status": 1
}
```

然后通过 `/xtimer/enableTimer` 激活。这样可以确保激活逻辑触发即时迁移，避免直接创建为激活状态却没有立即生成具体任务。

