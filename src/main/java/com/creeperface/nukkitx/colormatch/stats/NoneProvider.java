package com.creeperface.nukkitx.colormatch.stats;

import cn.nukkit.command.CommandSender;
import com.creeperface.nukkitx.colormatch.ColorMatch;

public class NoneProvider implements StatsProvider {

    private ColorMatch plugin;

    @Override
    public boolean init(ColorMatch plugin) {
        this.plugin = plugin;
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
    public void sendStats(String p, CommandSender receiver) {
        receiver.sendMessage(plugin.getLanguage().translateString("commands.failure.stats_disabled"));
    }

    @Override
    public void onDisable() {

    }
}
