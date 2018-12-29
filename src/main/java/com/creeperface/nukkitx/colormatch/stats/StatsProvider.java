package com.creeperface.nukkitx.colormatch.stats;

import cn.nukkit.Player;
import com.creeperface.nukkitx.colormatch.ColorMatch;

public interface StatsProvider {

    boolean init(ColorMatch plugin);

    void updateStats(String name, boolean win, int rounds);

    boolean createNewUser(String name);

    void sendStats(Player p);

    void onDisable();
}
