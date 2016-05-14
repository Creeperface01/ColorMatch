package main.java.ColorMatch.Arena;

import cn.nukkit.Server;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.level.generator.Generator;
import cn.nukkit.math.AxisAlignedBB;
import cn.nukkit.utils.Config;
import lombok.Getter;
import main.java.ColorMatch.ColorMatch;

public class Configuration {

    public static final int TYPE_NORMAL = 0;
    public static final int TYPE_FURIOUS = 1;
    public static final int TYPE_STONED = 2;
    public static final int TYPE_BLIND = 3;

    @Getter
    protected Position joinSign = null;

    @Getter
    protected Position leaveSign = null;

    @Getter
    protected Position startPos = null;

    @Getter
    protected Position spectatorPos = null;

    @Getter
    protected Position floorPos = null;

    @Getter
    protected AxisAlignedBB floor = new AxisAlignedBB(0, 0, 0, 0, 0, 0);

    @Getter
    protected int radius = 0;

    @Getter
    protected int type = TYPE_NORMAL;

    protected Level level;

    public boolean init(Config cfg) {
        Server server = Server.getInstance();
        String level = cfg.getString("arena_world");

        //arena level
        if (level.trim().equals("") || (server.getLevelByName(level) == null && !server.loadLevel(level))) {
            if (!server.generateLevel(level, 0, Generator.getGenerator("FLAT"))) {
                server.getLogger().warning(ColorMatch.getPrefix() + "An error occurred while loading level " + level + " in arena " + ((Arena) this).getName());
                return false;
            }

            this.level = server.getLevelByName(level);
        }

        //join sign level
        String joinLevel = cfg.getString("join_sign.world");

        if (joinLevel.trim().equals("") || (server.getLevelByName(joinLevel) == null && !server.loadLevel(joinLevel))) {
            if (!server.generateLevel(joinLevel, 0, Generator.getGenerator("FLAT"))) {
                server.getLogger().warning(ColorMatch.getPrefix() + "An error occurred while loading level " + joinLevel + " in arena " + ((Arena) this).getName());
                return false;
            }
        }

        joinSign = new Position(cfg.getInt("join_sign.x") + 0.5, cfg.getInt("join_sign.y"), cfg.getInt("join_sign.z") + 0.5, server.getLevelByName(joinLevel));
        leaveSign = new Position(cfg.getInt("leave_sign.x") + 0.5, cfg.getInt("leave_sign.y"), cfg.getInt("leave_sign.z") + 0.5, this.level);
        startPos = new Position(cfg.getInt("start_position.x") + 0.5, cfg.getInt("start_position.y"), cfg.getInt("start_position.z") + 0.5, this.level);
        spectatorPos = new Position(cfg.getInt("spectator_position.x") + 0.5, cfg.getInt("spectator_position.y"), cfg.getInt("spectator_position.z") + 0.5, this.level);
        floorPos = new Position(cfg.getInt("floor_position.x") + 0.5, cfg.getInt("floor_position.y"), cfg.getInt("floor_position.z") + 0.5, this.level);
        radius = cfg.getInt("floor_radius", 4);
        int type = cfg.getInt("type");

        if (type < 0 || type > 3) {
            server.getLogger().warning(ColorMatch.getPrefix() + "wrong arena type in arena " + ((Arena) this).getName());
            return false;
        }

        this.floor.setBounds(floorPos.getFloorX() - (radius * 3) - 1, floorPos.getFloorY(), floorPos.getFloorZ() - (radius * 3) - 1, floorPos.getFloorX() + (radius * 3) + 1, floorPos.getFloorY(), floorPos.getFloorZ() + (radius * 3) + 1);
        return true;
    }
}
