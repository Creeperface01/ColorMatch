package com.creeperface.nukkitx.colormatch.arena;

import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockCarpet;
import cn.nukkit.block.BlockTerracottaStained;
import cn.nukkit.block.BlockWool;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.level.generator.Generator;
import cn.nukkit.math.AxisAlignedBB;
import cn.nukkit.utils.Config;
import com.creeperface.nukkitx.colormatch.utils.Utils;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

public class Configuration extends Config {

    public static final int TYPE_NORMAL = 0;
    public static final int TYPE_FURIOUS = 1;
    public static final int TYPE_STONED = 2;
    public static final int TYPE_BLIND = 3;

    @Getter
    @Setter
    protected Position joinSign = null;

    @Getter
    @Setter
    protected Position startPos = null;

    @Getter
    @Setter
    protected Position spectatorPos = null;

    @Getter
    @Setter
    protected Position floorPos = null;

    @Getter
    @Setter
    protected AxisAlignedBB floor = null;

    @Getter
    @Setter
    protected int radius = 4;

    @Getter
    @Setter
    protected int type = 0;

    @Getter
    @Setter
    protected int colorChangeInterval = 5;

    @Getter
    @Setter
    protected Block floorMaterial = null;

    @Getter
    @Setter
    protected String floorType = "wool";

    @Getter
    @Setter
    protected Level level = null;

    @Getter
    @Setter
    protected String world = null;

    protected boolean init() {
        boolean right = true;

        Server server = Server.getInstance();

        if (exists("arena_world")) {

            String level = getString("arena_world");

            //arena level
            if (level.trim().equals("") || (server.getLevelByName(level) == null && !server.loadLevel(level) && !server.generateLevel(level, 0, Generator.getGenerator("FLAT")))) {
                server.getLogger().error("An error occurred while loading level " + level + " in arena " + ((Arena) this).getName());
                right = false;
            }

            this.level = server.getLevelByName(level);
        } else {
            right = false;
        }

        if (exists("join_sign") && exists("join_sign.world")) {
            //join sign level
            String joinLevel = getString("join_sign.world");

            if (joinLevel.trim().equals("") || (server.getLevelByName(joinLevel) == null && !server.loadLevel(joinLevel))) {
                if (!server.generateLevel(joinLevel, 0, Generator.getGenerator("FLAT"))) {
                    server.getLogger().error("An error occurred while loading level " + joinLevel + " in arena " + ((Arena) this).getName());
                    right = false;
                }
            }

            if (server.getLevelByName(joinLevel) != null) {
                joinSign = new Position(getInt("join_sign.x"), getInt("join_sign.y"), getInt("join_sign.z"), server.getLevelByName(joinLevel));
            }
        } else {
            right = false;
        }
        //leaveSign = new Position(getInt("leave_sign.x"), getInt("leave_sign.y"), getInt("leave_sign.z"), this.level);
        if (exists("start_position")) {
            startPos = new Position(getInt("start_position.x") + 0.5, getInt("start_position.y"), getInt("start_position.z") + 0.5, this.level);
        } else {
            right = false;
        }

        if (exists("spectator_position")) {
            spectatorPos = new Position(getInt("spectator_position.x") + 0.5, getInt("spectator_position.y"), getInt("spectator_position.z") + 0.5, this.level);
        } else {
            right = false;
        }

        if (exists("floor_position")) {
            floorPos = new Position(getInt("floor_position.x") + 0.5, getInt("floor_position.y"), getInt("floor_position.z") + 0.5, this.level);
        } else {
            right = false;
        }

        radius = getInt("floor_radius", 4);
        colorChangeInterval = getInt("color_change_interval", 5);

        int type = getInt("type", 0);

        if (type < 0 || type > 3) {
            server.getLogger().error("wrong arena type in arena " + ((Arena) this).getName());
            right = false;
        }

        this.type = type;

        String floorMaterial = getString("floor_type", "wool").toLowerCase();

        switch (floorMaterial) {
            case "wool":
                this.floorMaterial = new BlockWool();
                break;
            case "clay":
                this.floorMaterial = new BlockTerracottaStained();
                this.floorType = "clay";
                break;
            case "carpet":
                this.floorMaterial = new BlockCarpet();
                this.floorType = "carpet";
                break;
            default:
                this.floorMaterial = new BlockWool();
                server.getLogger().error("Unsupported floor material '" + floorMaterial + "' in arena " + ((Arena) this).getName());
                break;
        }

        recalculateBoundingBox();
        return right;
    }

    @Override
    public boolean save(Boolean async) {
        if (joinSign != null) {
            Utils.putPositionHelper(this, "join_sign", joinSign);
        }

        if (world != null) {
            set("arena_world", world);
        } else if (level != null) {
            set("arena_world", level.getFolderName());
        }

        if (startPos != null) Utils.putPositionHelper(this, "start_position", startPos);
        if (spectatorPos != null) Utils.putPositionHelper(this, "spectator_position", spectatorPos);
        if (floorPos != null) Utils.putPositionHelper(this, "floor_position", floorPos);

        set("floor_radius", radius);
        set("floor_type", floorType);
        set("color_change_interval", colorChangeInterval);
        set("type", type);

        return super.save(async);
    }

    public List<String> checkConfiguration() {
        List<String> fields = new ArrayList<>();

        if (joinSign == null) {
            fields.add("join sign");
        }

        if (startPos == null) {
            fields.add("start position");
        }

        if (spectatorPos == null) {
            fields.add("spectator position");
        }

        if (floorPos == null) {
            fields.add("floor position");
        }

        if (level == null && world == null) {
            fields.add("arena world");
        }

        return fields;
    }

    public void recalculateBoundingBox() {
        if (this.floorPos != null) {
            this.floor = new AxisAlignedBB(floorPos.getFloorX() - (radius * 3) - 1, floorPos.getFloorY(), floorPos.getFloorZ() - (radius * 3) - 1, floorPos.getFloorX() + (radius * 3) + 1, floorPos.getFloorY(), floorPos.getFloorZ() + (radius * 3) + 1);
        }
    }
}