package com.creeperface.nukkitx.colormatch.stats;

import cn.nukkit.Player;
import com.creeperface.nukkitx.colormatch.ColorMatch;

public class NoneProvider implements StatsProvider {

    @Override
    public boolean init(ColorMatch plugin) {
        return true;
    }

    @Override
    public void updateStats(String name, boolean win, int rounds) {

    }

    @Override
    public boolean createNewUser(String name) {
        return true;
    }

    @Override
    public void sendStats(Player p) {

    }

    @Override
    public void onDisable() {

    }
}
