package com.creeperface.nukkitx.colormatch.utils;

import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.item.Item;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.math.Vector3;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.ConfigSection;

import java.util.regex.Pattern;

public class Utils {

    public static String getLastColor(String msg) {
        int i = 0;
        char lastColor = 'f';

        for (char chr : msg.toCharArray()) {
            String str = Character.toString(chr);

            if (str.toLowerCase().equals("§")) {
                if ("0123456789AaBbCcDdEeFf".indexOf(msg.charAt(i + 1)) != -1) {
                    lastColor = msg.charAt(i + 1);
                }
            }
        }

        return String.valueOf(lastColor);
    }

    public static void putPositionHelper(Config cfg, String index, Vector3 v) {
        Level lvl = null;

        if (v instanceof Position) {
            lvl = ((Position) v).getLevel();
        }

        cfg.set(index + "." + "x", v.x);
        cfg.set(index + "." + "y", v.y);
        cfg.set(index + "." + "z", v.z);

        if (lvl != null) {
            cfg.set(index + "." + "world", lvl.getFolderName());
        }
    }

    public static Position getPositionHelper(ConfigSection cfg) {
        double x = cfg.getDouble("x");
        double y = cfg.getDouble("x");
        double z = cfg.getDouble("x");
        Level lvl = Server.getInstance().getLevelByName(cfg.getString("world"));

        return new Position(x, y, z, lvl);
    }

    public static Block fromString(String str) {
        String[] b = str.trim().replace(' ', '_').replace("minecraft:", "").split(":");

        int id = 0;
        int meta = 0;

        Pattern integerPattern = Pattern.compile("^[1-9]\\d*$");
        if (integerPattern.matcher(b[0]).matches()) {
            id = Integer.valueOf(b[0]);
        } else {
            try {
                id = Item.class.getField(b[0].toUpperCase()).getInt(null);
            } catch (Exception ignore) {
            }
        }

        id = id & 0xFFFF;
        if (b.length != 1) meta = Integer.valueOf(b[1]) & 0xFFFF;

        return Block.get(id, meta);
    }

    public static Vector3 positionToVector(Position pos) {
        return new Vector3(pos.x, pos.y, pos.z);
    }
}
