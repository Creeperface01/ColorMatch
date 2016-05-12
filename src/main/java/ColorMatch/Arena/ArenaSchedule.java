package main.java.ColorMatch.Arena;

import lombok.Getter;

public class ArenaSchedule implements Runnable {

    private Arena plugin;

    public ArenaSchedule(Arena plugin) {
        this.plugin = plugin;
    }

    public int time = 0;
    public int colorTime = 0;
    public int startTime = 0;

    @Getter
    public int id = 0;

    public boolean floor = false;

    public void run() {
        if (plugin.getPhase() == Arena.GAME) {
            game();
        } else {
            lobby();
        }
    }

    private void lobby() {
        startTime++;

        if (startTime >= plugin.plugin.conf.getStartTime()) {
            plugin.start();
        }
    }

    private void game() {
        if (time >= plugin.plugin.conf.getMaxGameTime()) {
            plugin.stop();
            return;
        }

        time++;

        if (floor) {
            if (colorTime % plugin.plugin.conf.getColorChangeInterval() == 0) {
                plugin.removeFloor();
                floor = false;
            }
        } else {
            if (colorTime % plugin.plugin.conf.getColorChangeInterval() == 0) {
                floor = true;
                plugin.selectNewColor();
                plugin.resetFloor();
            }
        }
    }
}
