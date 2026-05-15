drop table if exists xtimer;
create table xtimer (
                        timer_id bigint unsigned not null auto_increment,
                        create_time datetime not null default CURRENT_TIMESTAMP,
                        modify_time datetime not null default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
                        app varchar(128) collate utf8mb4_general_ci default null,
                        `name` varchar(128) collate utf8mb4_general_ci default null,
                        `status` TINYINT not null default '0' comment '1未激活，2激活',
                        cron varchar(256) collate utf8mb4_general_ci default null,
                        notify_http_param varchar(8192) collate utf8mb4_general_ci default null,
                        primary key(timer_id) using BTREE
)ENGINE=INNODB default charset=utf8mb4 collate=utf8mb4_general_ci;

drop table if exists timer_task;
create table timer_task(
                           task_id bigint unsigned not null auto_increment,
                           create_time datetime not null DEFAULT CURRENT_TIMESTAMP,
                           modify_time datetime not null default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
                           timer_id bigint unsigned not null,
                           app varchar(128) collate utf8mb4_general_ci default null,
                           output varchar(1028) collate utf8mb4_general_ci DEFAULT null,
                           `status` TINYINT not null default 0,
                           run_timer bigint,
                           cost_time bigint unsigned not null,
                           primary key(task_id) using btree,
                           unique key idx_timer_id_run_timer (timer_id,run_timer)
)ENGINE=INNODB default charset=utf8mb4 collate=utf8mb4_general_ci;