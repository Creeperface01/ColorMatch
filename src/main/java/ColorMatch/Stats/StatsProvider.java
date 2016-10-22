package ColorMatch.Stats;

import ColorMatch.ColorMatch;
import cn.nukkit.Player;

public interface StatsProvider {

    boolean init(ColorMatch plugin);

    void updateStats(String name, boolean win, int rounds);

    boolean createNewUser(String name);

    void sendStats(Player p);

    void onDisable();
}
