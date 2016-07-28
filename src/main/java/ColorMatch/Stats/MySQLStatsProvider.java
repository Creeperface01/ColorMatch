package ColorMatch.Stats;

import ColorMatch.ColorMatch;
import ColorMatch.Stats.MySQL.AsyncQuery;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.ConfigSection;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class MySQLStatsProvider implements StatsProvider {

    public static Map<String, Object> data = new HashMap<>();

    public static String hash = "ColorMatch.MySQL";

    @Override
    public boolean init(ColorMatch plugin) {
        Config cfg = plugin.conf;

        ConfigSection data = cfg.getSection("stats_provider_settings");

        MySQLStatsProvider.data = data.getAllMap();

        Connection connection = testConnection();

        if(connection == null) {
            Server.getInstance().getLogger().error("Could not connect to MySQL database!");
            return false;
        }

        try {
            PreparedStatement s = connection.prepareStatement("CREATE TABLE IF NOT EXISTS colormatch_stats ( name VARCHAR(16) PRIMARY KEY, wins INT, losses INT, rounds INT)");
            s.executeUpdate();
        }catch (SQLException e){
            Server.getInstance().getLogger().error("Could not create colormatch_stats table");
        }

        return true;
    }

    @Override
    public void updateStats(String name, boolean win, int rounds){
        UpdateStatsQuery updateStatsQuery = new UpdateStatsQuery(this, name, win ? 1 : 0, win ? 0 : 1, rounds);

        Server.getInstance().getScheduler().scheduleAsyncTask(updateStatsQuery);
    }

    @Override
    public boolean createNewUser(String name) {
        RegisterQuery query = new RegisterQuery(name);
        Server.getInstance().getScheduler().scheduleAsyncTask(query);
        return true;
    }

    @Override
    public void sendStats(Player p) {

    }

    private Connection testConnection(){
        String url = (String) data.get("host");
        String dbName = (String) data.get("database");
        String userName = (String) data.get("user");
        String password = (String) data.get("password");
        String port = (String) data.get("port");

        try {
            Class.forName("com.mysql.jdbc.Driver");
            return DriverManager.getConnection("jdbc:mysql://" + url + ":"+port+"/" + dbName, userName, password);
        } catch (SQLException ex) {
            ex.printStackTrace();
            return null;
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private class UpdateStatsQuery extends AsyncQuery {

        int deaths = 0;
        int win = 0;
        int rounds = 0;

        String name;

        MySQLStatsProvider plugin;

        private UpdateStatsQuery(MySQLStatsProvider plugin, String name, int win, int deaths, int rounds){
            this.win = win;
            this.deaths = deaths;
            this.rounds = rounds;
            this.plugin = plugin;
            this.name = name.toLowerCase();
        }

        @Override
        public void onRun(){
            Connection c = getMySQL();

            if(c == null){
                return;
            }

            try {
                PreparedStatement s = c.prepareStatement("UPDATE colormatch_stats SET wins = wins + "+win+", deaths = deaths + "+ deaths +", rounds = rounds + "+rounds+" WHERE name = '" + name + "'");
                s.executeUpdate();
            } catch (SQLException var41) {
                var41.printStackTrace();
            }
        }

        private Connection getMySQL(){
            Connection c = (Connection) getFromThreadStore(MySQLStatsProvider.hash);

            if(c == null){
                c = plugin.testConnection();

                if(c != null){
                    saveToThreadStore(MySQLStatsProvider.hash, c);
                }
            }

            return c;
        }
    }

    private class RegisterQuery extends AsyncQuery {

        String name;

        RegisterQuery(String name){
            this.name = name.toLowerCase();
        }

        @Override
        public void onQuery(Map<String, Object> data){
            if (data == null){
                try {
                    PreparedStatement e = getMySQLConnection().prepareStatement("INSERT INTO colormatch_stats (name, wins, deaths, rounds) VALUES ('" + name + "', '" + 0 + "', '" + 0 + "', '" + 0 + "')");
                    e.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
