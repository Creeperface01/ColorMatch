package com.creeperface.nukkitx.colormatch.stats;

import cn.nukkit.Player;
import cn.nukkit.Server;
import com.creeperface.nukkitx.colormatch.ColorMatch;
import com.creeperface.nukkitx.colormatch.stats.sql.AsyncQuery;
import ru.nukkit.dblib.DbLib;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

public abstract class SQLStatsProvider implements StatsProvider {

    public static final String hash = "colorMatch.sql";

    public static final String dbFileName = "stats.db";
    protected DbType databaseType;

    @Override
    public boolean init(ColorMatch plugin) {
        Connection connection = testConnection();

        if (connection == null) {
            Server.getInstance().getLogger().error("Could not connect to sql database!");
            return false;
        }

        try {
            PreparedStatement s = connection.prepareStatement("CREATE TABLE IF NOT EXISTS colormatch_stats ( name VARCHAR(16) PRIMARY KEY, wins INT, losses INT, rounds INT)");
            s.executeUpdate();
        } catch (SQLException e) {
            Server.getInstance().getLogger().error("Could not create colormatch_stats table");
        }

        return true;
    }

    @Override
    public void updateStats(String name, boolean win, int rounds) {
        UpdateStatsTask updateStatsTask = new UpdateStatsTask(this, name, win ? 1 : 0, win ? 0 : 1, rounds);

        Server.getInstance().getScheduler().scheduleAsyncTask(ColorMatch.getInstance(), updateStatsTask);
    }

    @Override
    public boolean createNewUser(String name) {
        RegisterQuery query = new RegisterQuery(name);
        Server.getInstance().getScheduler().scheduleAsyncTask(ColorMatch.getInstance(), query);
        return true;
    }

    @Override
    public void sendStats(Player p) {
        GetStatsQuery query = new GetStatsQuery(p.getName());
        Server.getInstance().getScheduler().scheduleAsyncTask(ColorMatch.getInstance(), query);
    }

    private Connection testConnection() {
        try {
            if (databaseType == DbType.MYSQL) {
                Class.forName("com.mysql.jdbc.Driver");
                return DriverManager.getConnection(DbLib.getUrlFromConfig(null));
            } else {
                return DbLib.getSQLiteConnection(ColorMatch.getInstance(), dbFileName);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            return null;
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private class UpdateStatsTask extends AsyncQuery {

        int deaths = 0;
        int win = 0;
        int rounds = 0;

        String name;

        SQLStatsProvider plugin;

        private UpdateStatsTask(SQLStatsProvider plugin, String name, int win, int deaths, int rounds) {
            this.win = win;
            this.deaths = deaths;
            this.rounds = rounds;
            this.plugin = plugin;
            this.name = name.toLowerCase();
            this.databaseType = plugin.databaseType;
        }

        @Override
        public void onRun() {
            Connection c = getMySQLConnection();

            if (c == null) {
                return;
            }

            try {
                PreparedStatement s = c.prepareStatement("UPDATE colormatch_stats SET wins = wins + " + win + ", deaths = deaths + " + deaths + ", rounds = rounds + " + rounds + " WHERE name = '" + name + "'");
                s.executeUpdate();
            } catch (SQLException var41) {
                var41.printStackTrace();
            }
        }
    }

    private class RegisterQuery extends AsyncQuery {

        String name;

        RegisterQuery(String name) {
            this.name = name.toLowerCase();
        }

        @Override
        public void onQuery(Map<String, Object> data) {
            if (data == null) {
                try {
                    PreparedStatement e = getMySQLConnection().prepareStatement("INSERT INTO colormatch_stats (name, wins, deaths, rounds) VALUES ('" + name + "', '" + 0 + "', '" + 0 + "', '" + 0 + "')");
                    e.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class GetStatsQuery extends AsyncQuery {

        GetStatsQuery(String name) {
            player = name;
        }

        Map<String, Object> data = null;

        @Override
        public void onQuery(Map<String, Object> data) {
            this.data = data;
        }

        @Override
        public void onCompletion(Server server) {
            if (data == null) {
                return;
            }

            Player p = server.getPlayerExact(player);

            if (p == null || !p.isOnline()) {
                return;
            }

            ColorMatch plugin = ColorMatch.getInstance();

            String msg = plugin.getLanguage().translateString("commands.success.stats", p.getName());
            msg += "\n" + plugin.getLanguage().translateString("stats.wins", String.valueOf(data.get("wins")));
            msg += "\n" + plugin.getLanguage().translateString("stats.deaths", String.valueOf(data.get("deaths")));
            msg += "\n" + plugin.getLanguage().translateString("stats.rounds", String.valueOf(data.get("rounds")));

            p.sendMessage(msg);
        }
    }

    @Override
    public void onDisable() {

    }

    public enum DbType {
        MYSQL,
        SQLITE
    }
}
