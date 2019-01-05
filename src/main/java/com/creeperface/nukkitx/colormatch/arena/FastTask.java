package com.creeperface.nukkitx.colormatch.arena;

import cn.nukkit.utils.MainLogger;
import lombok.RequiredArgsConstructor;

/**
 * @author CreeperFace
 */
@RequiredArgsConstructor
public class FastTask implements Runnable {

    private final Arena plugin;

    @Override
    public void run() {
        if (plugin.getPhase() == Arena.PHASE_GAME) {
            ArenaSchedule scheduler = plugin.scheduler;

            if (scheduler.floor) {
                int interval = plugin.getColorChangeInterval() * 10;
                int tick = plugin.plugin.getServer().getTick();
                int begin = scheduler.floorResetedTick;

                this.plugin.bossBar.setHealth(interval - ((tick - begin) / 2));
                this.plugin.bossBar.updateHealth();

                MainLogger.getLogger().info("progress: " + (interval - (((tick - begin) / 2) * interval)) + "/" + (interval * 10));
            }
        }
    }
}
