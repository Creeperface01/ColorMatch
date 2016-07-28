package ColorMatch.Stats.MySQL;

import ColorMatch.Stats.MySQLStatsProvider;
import cn.nukkit.scheduler.AsyncTask;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

import static ColorMatch.Stats.MySQLStatsProvider.data;

public class AsyncQuery extends AsyncTask {

    protected String player;

    @Override
    public void onRun(){
        onQuery(getData(player.toLowerCase().trim()));
    }

    public void onQuery(Map<String, Object> data){

    }

    private Map<String, Object> getData(String player){
        try {
            Connection c = getMySQLConnection();

            if(c == null){
                return null;
            }

            PreparedStatement s = c.prepareStatement("SELECT * FROM colormatch_stats WHERE name = '" + player.toLowerCase().trim() + "'");
            ResultSet result = s.executeQuery();
            ResultSetMetaData md = result.getMetaData();
            int columns = md.getColumnCount();
            HashMap row = new HashMap();

            while(result.next()) {
                for(int i = 1; i <= columns; ++i) {
                    row.put(md.getColumnName(i), result.getObject(i));
                }
            }

            if(row.isEmpty()){
                return null;
            }

            return row;
        } catch (SQLException var91) {
            var91.printStackTrace();
            return null;
        }
    }

    protected Connection getMySQLConnection(){
        Connection c = (Connection) getFromThreadStore(MySQLStatsProvider.hash);

        if(c == null){
            c = connect();

            if(c != null){
                saveToThreadStore(MySQLStatsProvider.hash, c);
            }
        }

        return c;
    }

    private Connection connect(){
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
}
