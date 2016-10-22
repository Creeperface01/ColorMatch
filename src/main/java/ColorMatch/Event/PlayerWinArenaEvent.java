package ColorMatch.Event;

import ColorMatch.Arena.Arena;
import ColorMatch.Utils.Reward;
import cn.nukkit.Player;
import cn.nukkit.event.HandlerList;
import cn.nukkit.event.player.PlayerEvent;
import lombok.Getter;

public class PlayerWinArenaEvent extends PlayerEvent {
    private static final HandlerList handlers = new HandlerList();

    @Getter
    private Arena arena = null;

    @Getter
    private Reward reward = null;

    public static HandlerList getHandlers() {
        return handlers;
    }

    public PlayerWinArenaEvent(Player p, Arena a, Reward reward) {
        this.player = p;
        this.arena = a;
        this.reward = reward;
    }
}
