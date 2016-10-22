package ColorMatch.Stats;

import ColorMatch.ColorMatch;
import cn.nukkit.Player;

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
