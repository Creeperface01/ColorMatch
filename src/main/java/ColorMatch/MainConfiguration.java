package main.java.ColorMatch;

import cn.nukkit.Server;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.utils.Config;
import lombok.Getter;
import main.java.ColorMatch.Arena.Arena;

public class MainConfiguration {

    @Getter
    private int minPlayers = 0;

    @Getter
    private int maxPlayers = 0;

    @Getter
    private Position lobby = null;

    @Getter
    private int colorChangeInterval = 0;

    @Getter
    private boolean directJoin = false;

    @Getter
    private int maxGameTime = 0;

    @Getter
    private int startTime = 0;

    public boolean init(Config cfg) {
        Server server = Server.getInstance();


        minPlayers = cfg.getInt("min_players", 4);
        maxPlayers = cfg.getInt("max_players", 12);
        maxGameTime = cfg.getInt("max_game_time", 600);
        startTime = cfg.getInt("start_time", 30);

        String name = cfg.getString("lobby.world");

        Level level = server.getLevelByName(name);

        if (level == null && !server.loadLevel(name)) {
            server.getLogger().warning(ColorMatch.getPrefix() + "level " + name + " doesn't exist");
            return false;
        }

        return true;
    }
}
