package com.creeperface.nukkitx.colormatch.arena;

import cn.nukkit.Player;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class ArenaSchedule implements Runnable {

    private final Arena plugin;

    public int time = 0;
    public int colorTime = 0;
    public int startTime = 0;

    @Getter
    public int id = 0;

    public boolean floor = true;

    public int floorResetedTick;

    public void run() {
        if (plugin.getPhase() == Arena.PHASE_GAME) {
            game();
        } else {
            lobby();
        }
    }

    private void lobby() {
        if (plugin.starting) {
            int maxTime = plugin.plugin.conf.getStartTime();

            startTime++;

            plugin.players.values().forEach((Player p) -> p.setExperience(0, maxTime - startTime));

            if (startTime >= plugin.plugin.conf.getStartTime()) {
                plugin.start();
                startTime = 0;
            }
        }
    }

    private void game() {
        if (time >= plugin.plugin.conf.getMaxGameTime()) {
            plugin.stop();
            return;
        }

        int interval = plugin.getColorChangeInterval();

        if (colorTime > 0 && (colorTime % interval) == 0) {
            if (floor) {
                plugin.removeFloor();
                floor = false;
                this.plugin.bossBar.setHealth(interval * 10);
                this.plugin.bossBar.updateInfo();
            } else {
                floor = true;
                plugin.selectNewColor();
                plugin.resetFloor();
                floorResetedTick = plugin.plugin.getServer().getTick();
            }
        }

//        if(floor) {
//            update = true;
//            this.plugin.bossBar.setHealth((interval - 1) - (colorTime % (interval - 1)));
//        }

        time++;
        colorTime++;
    }
}
