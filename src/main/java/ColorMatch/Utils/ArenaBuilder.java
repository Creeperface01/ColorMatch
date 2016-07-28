package ColorMatch.Utils;

import ColorMatch.Arena.Arena;
import ColorMatch.ColorMatch;
import cn.nukkit.block.*;
import cn.nukkit.level.Level;
import cn.nukkit.math.AxisAlignedBB;
import cn.nukkit.math.Vector3;
import cn.nukkit.utils.TextFormat;

public class ArenaBuilder {

    public static String build(Arena cfg, Level level) {
        return build(cfg, level, new BlockIron());
    }

    public static String build(Arena cfg, Level level, Block b) {
        long time = System.currentTimeMillis();

        AxisAlignedBB bb = cfg.getFloor();

        int minX = (int) bb.minX - 1;
        int minY = (int) bb.minY;
        int minZ = (int) bb.minZ - 1;
        int maxX = (int) bb.maxX + 1;
        int maxY = (int) bb.maxY;
        int maxZ = (int) bb.maxZ + 1;

        if (minY < 5) {
            return ColorMatch.getPrefix() + TextFormat.RED + "Can not generate. Floor Y is too low";
        }

        int lavaY;
        int lava2Y = -1;

        int roofY = (int) cfg.getSpectatorPos().y + 4;

        int spectatorY = (int) cfg.getSpectatorPos().y - 1;
        int floorY;

        if (minY > 10) {
            lavaY = minY - 9;
            lava2Y = minY - 10;
            floorY = minY - 11;
        } else {
            lavaY = minY - 4;
            floorY = minY - 5;
        }

        Vector3 v = new Vector3();
        int blocks = 0;

        /**
         * clear area
         */

        Block air = new BlockAir();

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                for(int y = floorY; y <= roofY; y++){
                    level.setBlock(v.setComponents(x, y, z), air, true, false);
                }
            }
        }

        /**
         * generate wall
         */

        for (int y = floorY; y <= roofY; y++) {
            for (int x = minX; x <= maxX; x++) {
                level.setBlock(v.setComponents(x, y, minZ), b, true, false);
                level.setBlock(v.setComponents(x, y, maxZ), b, true, false);
                blocks += 2;
            }

            for (int z = minZ; z <= maxZ; z++) {
                level.setBlock(v.setComponents(minX, y, z), b, true, false);
                level.setBlock(v.setComponents(maxX, y, z), b, true, false);
                blocks += 2;
            }
        }

        /**
         * generate spectator floor, floor, lava and roof
         */

        Block glass = new BlockGlass();
        Block lava = new BlockLavaStill();

        for (int x = minX + 1; x <= maxX - 1; x++) {
            for (int z = minZ + 1; z <= maxZ - 1; z++) {
                level.setBlock(v.setComponents(x, spectatorY, z), glass, true, false);
                level.setBlock(v.setComponents(x, floorY, z), b, true, false);
                level.setBlock(v.setComponents(x, roofY, z), glass, true, false);
                level.setBlock(v.setComponents(x, lavaY, z), lava, true, false);

                if (lava2Y != -1) {
                    level.setBlock(v.setComponents(x, lava2Y, z), lava, true, false);
                    blocks++;
                }

                blocks += 4;
            }
        }

        cfg.resetFloor();

        return ColorMatch.getPrefix() + TextFormat.GREEN + "Arena was successfully generated " + TextFormat.GRAY + "(" + blocks + " blocks, " + (System.currentTimeMillis() - time) / 1000 + " seconds)";
    }
}
