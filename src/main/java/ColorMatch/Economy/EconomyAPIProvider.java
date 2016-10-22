package ColorMatch.Economy;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.plugin.Plugin;
import me.onebone.economyapi.EconomyAPI;

public class EconomyAPIProvider implements EconomyProvider {

    private EconomyAPI plugin = null;

    public EconomyAPIProvider(Plugin plugin) {
        if (plugin instanceof EconomyAPI) {
            this.plugin = (EconomyAPI) plugin;
        } else {
            Server.getInstance().getLogger().warning("Could not register plugin Economy");
        }

    }

    @Override
    public void addMoney(Player p, double money) {
        plugin.addMoney(p, money);
    }
}
