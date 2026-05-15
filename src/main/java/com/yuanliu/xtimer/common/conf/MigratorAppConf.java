package com.yuanliu.xtimer.common.conf;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * ClassName:
 * Package: com.yuanliu.xtimer.common.conf
 * Description:
 *
 * @Author Yuan Liu
 * @Create 2026/5/13 15:32
 * @Version 1.0
 */
@Component
public class MigratorAppConf {

    @Value("60")
    private int migrateStepMinutes;

    @Value("1")
    private int migrateTryLockMinutes;

    public int getMigrateStepMinutes() {
        return migrateStepMinutes;
    }

    public void setMigrateStepMinutes(int migrateStepMinutes) {
        this.migrateStepMinutes = migrateStepMinutes;
    }

    public int getMigrateTryLockMinutes() {
        return migrateTryLockMinutes;
    }

    public void setMigrateTryLockMinutes(int migrateTryLockMinutes) {
        this.migrateTryLockMinutes = migrateTryLockMinutes;
    }
}
