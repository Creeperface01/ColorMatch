package com.creeperface.nukkitx.colormatch;

import cn.nukkit.Server;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.level.generator.Generator;
import cn.nukkit.utils.ConfigSection;
import cn.nukkit.utils.SimpleConfig;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

public class MainConfiguration extends SimpleConfig {

    @Setter
    @Getter
    @Path("min_players")
    private int minPlayers = 0;

    @Setter
    @Getter
    @Path("max_players")
    private int maxPlayers = 0;

    @Setter
    @Getter
    @Skip
    private Position mainLobby = null;

    @Path("main_lobby")
    private ConfigSection lobby = null;

    @Setter
    @Getter
    private boolean directJoin = false;

    @Setter
    @Getter
    @Path("max_game_time")
    private int maxGameTime = 0;

    @Setter
    @Getter
    @Path("start_time")
    private int startTime = 0;

    @Setter
    @Getter
    @Path("save_inventory")
    private boolean saveInventory = true;

    @Getter
    @Path("language")
    private String language = "english";

    @Getter
    @Path("stats_provider")
    private String stats = "none";

    @Getter
    @Path("reward")
    private ConfigSection reward = null;

    @Getter
    @Path("chat.game")
    private String gameChatFormat = "&7[&aGAME&7]&e&r {PLAYER} &3>&7 {MESSAGE}";

    @Getter
    @Path("chat.spectator")
    private String spectatorChatFormat = "&7[&eSPECTATE&7]&e&r {PLAYER} &3>&7 {MESSAGE}";

    public MainConfiguration(ColorMatch plugin) {
        super(plugin);
        //language = "English";
        //stats = "none";
        //saveInventory = true;
    }

    public boolean load(boolean isFirst) {
        if (!super.load()) {
            return false;
        }

        Server server = Server.getInstance();

        if (isFirst || lobby == null) {
            mainLobby = server.getDefaultLevel().getSafeSpawn();

            lobby = new ConfigSection();
            lobby.set("world", mainLobby.getLevel().getName());
            lobby.set("x", mainLobby.x);
            lobby.set("y", mainLobby.y);
            lobby.set("z", mainLobby.z);
        } else {
            String name = lobby.getString("world");
            Level level;

            if (name.trim().isEmpty()) {
                level = server.getDefaultLevel();
            } else {
                level = server.getLevelByName(name);
            }

            if (level == null && !server.loadLevel(name) && !server.generateLevel(name, 0, Generator.getGenerator(Generator.TYPE_FLAT))) {
                ColorMatch.getInstance().getLogger().warning("level " + name + " doesn't exist");
                return false;
            }

            level = server.getLevelByName(name);

            mainLobby = new Position(lobby.getDouble("main_lobby.x"), lobby.getDouble("main_lobby.y"), lobby.getDouble("main_lobby.z"), level);
        }

        gameChatFormat = gameChatFormat.replaceAll("&", "§");
        spectatorChatFormat = spectatorChatFormat.replaceAll("&", "§");

        if (reward == null || reward.isEmpty()) {
            reward = new ConfigSection();
            reward.set("enable", false);
            reward.set("money", 0.0);
            reward.set("items", new ArrayList<>());
        }

        return true;
    }

    @Override
    public boolean save() {
        lobby.set("world", mainLobby.getLevel().getName());
        lobby.set("x", mainLobby.x);
        lobby.set("y", mainLobby.y);
        lobby.set("z", mainLobby.z);

        return super.save();
    }

    /*public boolean init(String file, boolean isFirst) {
        if(!load(file, Config.YAML)){
            return false;
        }

        if(getInt("version") < 2){
            set("stats_provider", "none");
            set("reward.enable", true);
            set("reward.money", 0.0);
            set("reward.items", new Object[0]);
        }

        Server server = Server.getInstance();
        colorMatch plugin = colorMatch.getInstance();

        language = getString("language", "english").toLowerCase();

        stats = getString("stats_provider", "yaml");

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
    }*/

    /*@Override
    public boolean save() {
        set("language", language);
        utils.putPositionHelper(this, "main_lobby", mainLobby);
        set("min_players", );
        set("max_players", );
        set("max_game_time", );
        set("start_time", );
        set("save_inventory", );

        return super.save();
    }*/
}
