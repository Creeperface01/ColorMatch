package ColorMatch.Stats;

import ColorMatch.ColorMatch;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.ConfigSection;

public class YamlStatsProvider implements StatsProvider {

    Config cfg = null;

    @Override
    public boolean init(ColorMatch plugin) {
        cfg = new Config(plugin.getDataFolder()+"/stats.yml", Config.YAML);
        Server.getInstance().getScheduler().scheduleDelayedRepeatingTask(new SaveTask(this), 300 * 20, 300 * 20);
        return true;
    }

    @Override
    public void updateStats(String name, boolean win, int rounds) {
        ConfigSection data = cfg.getSection(name.toLowerCase());

        if(win) {
            data.set("wins", data.getInt("wins") + 1);
        } else {
            data.set("deaths", data.getInt("deaths") + 1);
        }

        data.set("rounds", data.getInt("rounds") + rounds);
    }

    @Override
    public boolean createNewUser(String name) {
        if(cfg.exists(name.toLowerCase())){
            return false;
        }

        ConfigSection data = new ConfigSection();
        data.set("wins", 0);
        data.set("deaths", 0);
        data.set("rounds", 0);

        cfg.set(name.toLowerCase(), data);
        return true;
    }

    @Override
    public void sendStats(Player p) {
        ConfigSection data = cfg.getSection(p.getName().toLowerCase());

        if(data == null){
            return;
        }

        ColorMatch plugin = ColorMatch.getInstance();

        String msg = plugin.getLanguage().translateString("commands.success.stats", p.getName());
        msg += "\n" + plugin.getLanguage().translateString("stats.wins", data.getString("wins"));
        msg += "\n" + plugin.getLanguage().translateString("stats.deaths", data.getString("deaths"));
        msg += "\n" + plugin.getLanguage().translateString("stats.rounds", data.getString("rounds"));

        p.sendMessage(msg);
    }

    private class SaveTask implements Runnable {

        YamlStatsProvider plugin;

        SaveTask(YamlStatsProvider plugin){
            this.plugin = plugin;
        }

        public void run(){
            plugin.cfg.save(true);
        }
    }
}
