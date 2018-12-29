package com.creeperface.nukkitx.colormatch.economy;

import cc.leet.economy.Economy;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.plugin.Plugin;

public class LeetEconomyProvider implements EconomyProvider {

    private Economy plugin = null;

    public LeetEconomyProvider(Plugin plugin) {
        if (plugin instanceof Economy) {
            this.plugin = (Economy) plugin;
        } else {
            Server.getInstance().getLogger().warning("Could not register plugin economy");
        }

    }

    @Override
    public void addMoney(Player p, double money) {
        plugin.getAPI().alterBalance(p.getName(), money);
    }
}
