package com.creeperface.nukkitx.colormatch.arena;

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
                float interval = plugin.getColorChangeInterval() * 20;
                float tick = plugin.plugin.getServer().getTick();
                float begin = scheduler.floorResetedTick;

                this.plugin.bossBar.setPercent(1 - ((tick - begin) / interval));
                this.plugin.bossBar.updateData();
            }
        }
    }
}
