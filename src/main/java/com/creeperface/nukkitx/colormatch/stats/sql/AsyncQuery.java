package com.creeperface.nukkitx.colormatch.stats.sql;

import cn.nukkit.scheduler.AsyncTask;
import com.creeperface.nukkitx.colormatch.stats.SQLStatsProvider.DbType;
import lombok.Cleanup;
import ru.nukkit.dblib.DbLib;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class AsyncQuery extends AsyncTask {

    protected String player;
    protected DbType databaseType;

    @Override
    public void onRun() {
        onQuery(getData(player.toLowerCase().trim()));
    }

    public void onQuery(Map<String, Object> data) {

    }

    private Map<String, Object> getData(String player) {
        try {
            @Cleanup Connection c = getMySQLConnection();

            if (c == null) {
                return null;
            }

            @Cleanup PreparedStatement s = c.prepareStatement("SELECT * FROM colormatch_stats WHERE name = ?");
            s.setString(1, player.toLowerCase().trim());

            @Cleanup ResultSet result = s.executeQuery();
            ResultSetMetaData md = result.getMetaData();
            int columns = md.getColumnCount();
            HashMap<String, Object> row = new HashMap<>();

            while (result.next()) {
                for (int i = 1; i <= columns; ++i) {
                    row.put(md.getColumnName(i), result.getObject(i));
                }
            }

            if (row.isEmpty()) {
                return null;
            }

            return row;
        } catch (SQLException var91) {
            var91.printStackTrace();
            return null;
        }
    }

    protected Connection getMySQLConnection() {
        return DbLib.getDefaultConnection();
//        Connection c;

//        if (databaseType == DbType.MYSQL) {
//            c = connect();
//        } else {
//            c = DbLib.getSQLiteConnection(ColorMatch.getInstance(), SQLStatsProvider.dbFileName);
//        }

//        return c;
    }

//    protected Connection getSQLite3Connection() {
//        return DbLib.getSQLiteConnection(ColorMatch.getInstance(), "stats.db");
//    }

//    private Connection connect() {
//        try {
//            Class.forName("com.mysql.jdbc.Driver");
//            return DriverManager.getConnection(DbLib.getUrlFromConfig(null));
//        } catch (SQLException | ClassNotFoundException ex) {
//            ex.printStackTrace();
//            return null;
//        }
//    }
}
