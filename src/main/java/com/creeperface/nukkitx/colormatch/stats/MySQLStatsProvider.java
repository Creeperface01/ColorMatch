package com.creeperface.nukkitx.colormatch.stats;

/**
 * Created by CreeperFace on 1.7.2017.
 */
public class MySQLStatsProvider extends SQLStatsProvider {

    public MySQLStatsProvider() {
        this.databaseType = DbType.MYSQL;
    }
}
