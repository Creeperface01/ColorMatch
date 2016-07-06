package ColorMatch;

import cn.nukkit.Server;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.utils.Config;
import lombok.Getter;
import lombok.Setter;

import java.io.File;

public class MainConfiguration extends Config{

    @Setter
    @Getter
    private int minPlayers = 0;

    @Setter
    @Getter
    private int maxPlayers = 0;

    @Setter
    @Getter
    private Position mainLobby = null;

    @Setter
    @Getter
    private boolean directJoin = false;

    @Setter
    @Getter
    private int maxGameTime = 0;

    @Setter
    @Getter
    private int startTime = 0;

    @Setter
    @Getter
    private boolean saveInventory = true;

    public boolean init(String file, boolean isFirst) {
        if(!load(file, Config.YAML)){
            return false;
        }

        Server server = Server.getInstance();

        minPlayers = getInt("min_players", 4);
        maxPlayers = getInt("max_players", 12);
        maxGameTime = getInt("max_game_time", 600);
        startTime = getInt("start_time", 30);
        saveInventory = getBoolean("save_inventory", true);

        if(isFirst){
            mainLobby = server.getDefaultLevel().getSafeSpawn();
        } else {
            String name = getString("main_lobby.world", server.getDefaultLevel().getName());
            Level level;

            if (name.trim().equals("")) {
                level = server.getDefaultLevel();
            } else {
                level = server.getLevelByName(name);
            }

            if (level == null && !server.loadLevel(name)) {
                server.getLogger().warning("level " + name + " doesn't exist");
                return false;
            }

            mainLobby = new Position(getInt("main_lobby.x"), getInt("main_lobby.y"), getInt("main_lobby.z"), level);
        }

        return true;
    }

    @Override
    public boolean save(){


        return super.save();
    }
}
