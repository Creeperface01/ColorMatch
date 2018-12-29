package com.creeperface.nukkitx.colormatch.arena;

import cn.nukkit.Player;
import lombok.Getter;

class ArenaSchedule implements Runnable {

    private Arena plugin;

    public ArenaSchedule(Arena plugin) {
        this.plugin = plugin;
    }

    public int time = 0;
    public int colorTime = 0;
    public int startTime = 0;

    @Getter
    public int id = 0;

    public boolean floor = true;

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

        if (colorTime > 0 && (colorTime % plugin.getColorChangeInterval()) == 0) {
            if (floor) {
                plugin.removeFloor();
                floor = false;
            } else {
                floor = true;
                plugin.selectNewColor();
                plugin.resetFloor();
            }
        }

        time++;
        colorTime++;
    }
}
