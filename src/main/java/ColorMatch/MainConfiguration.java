package ColorMatch;

import cn.nukkit.Server;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.utils.Config;
import lombok.Getter;

public class MainConfiguration {

    @Getter
    private int minPlayers = 0;

    @Getter
    private int maxPlayers = 0;

    @Getter
    private Position mainLobby = null;

    @Getter
    private boolean directJoin = false;

    @Getter
    private int maxGameTime = 0;

    @Getter
    private int startTime = 0;

    @Getter
    private boolean saveInventory = true;

    public boolean init(Config cfg) {
        Server server = Server.getInstance();

        minPlayers = cfg.getInt("min_players", 4);
        maxPlayers = cfg.getInt("max_players", 12);
        maxGameTime = cfg.getInt("max_game_time", 600);
        startTime = cfg.getInt("start_time", 30);
        saveInventory = cfg.getBoolean("save_inventory", true);

        String name = cfg.getString("main_lobby.world", server.getDefaultLevel().getName());

        if (name.trim().equals("")) {
            name = server.getDefaultLevel().getName();
        }

        Level level = server.getLevelByName(name);

        if (level == null && !server.loadLevel(name)) {
            server.getLogger().warning("level " + name + " doesn't exist");
            return false;
        }

        if(cfg.exists("main_lobby")) {
            mainLobby = new Position(cfg.getInt("main_lobby.x"), cfg.getInt("main_lobby.y"), cfg.getInt("main_lobby.z"), level);
        } else {
            mainLobby = server.getDefaultLevel().getSpawnLocation();
        }
        return true;
    }
}
