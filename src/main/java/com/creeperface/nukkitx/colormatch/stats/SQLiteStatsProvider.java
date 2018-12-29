package com.creeperface.nukkitx.colormatch.stats;

/**
 * Created by CreeperFace on 1.7.2017.
 */
public class SQLiteStatsProvider extends SQLStatsProvider {

    public SQLiteStatsProvider() {
        this.databaseType = DbType.SQLITE;
    }
}
