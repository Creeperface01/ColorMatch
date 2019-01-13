package com.creeperface.nukkitx.colormatch.stats;

import cn.nukkit.Server;
import cn.nukkit.command.CommandSender;
import com.creeperface.nukkitx.colormatch.ColorMatch;
import com.creeperface.nukkitx.colormatch.stats.sql.AsyncQuery;
import lombok.Cleanup;
import ru.nukkit.dblib.DbLib;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

public abstract class SQLStatsProvider implements StatsProvider {

    public static final String dbFileName = "stats.db";
    protected DbType databaseType;

    @Override
    public boolean init(ColorMatch plugin) {
        try (Connection connection = getConnection()) {
            @Cleanup PreparedStatement s = connection.prepareStatement("CREATE TABLE IF NOT EXISTS colormatch_stats ( name VARCHAR(50) PRIMARY KEY, wins INT, losses INT, rounds INT)");
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
    public void sendStats(String p, CommandSender receiver) {
        GetStatsQuery query = new GetStatsQuery(p, receiver);
        Server.getInstance().getScheduler().scheduleAsyncTask(ColorMatch.getInstance(), query);
    }

    private Connection getConnection() {
        try {
            if (databaseType == DbType.MYSQL) {
                Class.forName("com.mysql.jdbc.Driver");
                return DriverManager.getConnection(DbLib.getUrlFromConfig(null));
            } else {
                return DbLib.getSQLiteConnection(ColorMatch.getInstance(), dbFileName);
            }
        } catch (SQLException | ClassNotFoundException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private class UpdateStatsTask extends AsyncQuery {

        private final int deaths;
        private final int wins;
        private final int rounds;

        private final String name;

        private final SQLStatsProvider plugin;

        private UpdateStatsTask(SQLStatsProvider plugin, String name, int wins, int deaths, int rounds) {
            this.wins = wins;
            this.deaths = deaths;
            this.rounds = rounds;
            this.plugin = plugin;
            this.name = name.toLowerCase();
            this.databaseType = plugin.databaseType;
        }

        @Override
        public void onRun() {
            try (Connection c = getMySQLConnection()) {
                @Cleanup PreparedStatement s = c.prepareStatement("UPDATE colormatch_stats SET wins = wins + ?, deaths = deaths + ?, rounds = rounds + ? WHERE name = ?");
                s.setInt(1, wins);
                s.setInt(2, deaths);
                s.setInt(3, rounds);
                s.setString(4, name);

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
                try (Connection con = getMySQLConnection()) {
                    @Cleanup PreparedStatement e = con.prepareStatement("INSERT INTO colormatch_stats (name, wins, deaths, rounds) VALUES (?, ?, ?, ?)");
                    e.setString(1, name);
                    e.setInt(2, 0);
                    e.setInt(3, 0);
                    e.setInt(4, 0);

                    e.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class GetStatsQuery extends AsyncQuery {

        private CommandSender receiver;

        GetStatsQuery(String name, CommandSender receiver) {
            player = name;
            this.receiver = receiver;
        }

        Map<String, Object> data = null;

        @Override
        public void onQuery(Map<String, Object> data) {
            this.data = data;
        }

        @Override
        public void onCompletion(Server server) {
            if (data == null) {
                receiver.sendMessage(ColorMatch.getInstance().getLanguage().translateString("commands.failure.stats_player", player));
                return;
            }

            ColorMatch plugin = ColorMatch.getInstance();

            String msg = plugin.getLanguage().translateString("commands.success.stats", player);
            msg += "\n" + plugin.getLanguage().translateString("stats.wins", String.valueOf(data.get("wins")));
            msg += "\n" + plugin.getLanguage().translateString("stats.deaths", String.valueOf(data.get("deaths")));
            msg += "\n" + plugin.getLanguage().translateString("stats.rounds", String.valueOf(data.get("rounds")));

            receiver.sendMessage(msg);
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
