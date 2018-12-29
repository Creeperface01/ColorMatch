package com.creeperface.nukkitx.colormatch.stats.sql;

import cn.nukkit.scheduler.AsyncTask;
import com.creeperface.nukkitx.colormatch.ColorMatch;
import com.creeperface.nukkitx.colormatch.stats.SQLStatsProvider;
import com.creeperface.nukkitx.colormatch.stats.SQLStatsProvider.DbType;
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
            Connection c = getMySQLConnection();

            if (c == null) {
                return null;
            }

            PreparedStatement s = c.prepareStatement("SELECT * FROM colormatch_stats WHERE name = '" + player.toLowerCase().trim() + "'");
            ResultSet result = s.executeQuery();
            ResultSetMetaData md = result.getMetaData();
            int columns = md.getColumnCount();
            HashMap row = new HashMap();

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
        Connection c;


        if (databaseType == DbType.MYSQL) {
            c = (Connection) getFromThreadStore(SQLStatsProvider.hash);

            if (c == null) {
                c = connect();

                if (c != null) {
                    saveToThreadStore(SQLStatsProvider.hash, c);
                }
            }
        } else {
            c = DbLib.getSQLiteConnection(ColorMatch.getInstance(), SQLStatsProvider.dbFileName);
        }

        return c;
    }

    protected Connection getSQLite3Connection() {
        return DbLib.getSQLiteConnection(ColorMatch.getInstance(), "stats.db");
    }

    private Connection connect() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            return DriverManager.getConnection(DbLib.getUrlFromConfig(null));
        } catch (SQLException ex) {
            ex.printStackTrace();
            return null;
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
