package ColorMatch.Event;

import ColorMatch.Arena.Arena;
import cn.nukkit.Player;
import cn.nukkit.event.HandlerList;
import cn.nukkit.event.player.PlayerEvent;
import lombok.Getter;

public class PlayerJoinSpectatorEvent extends PlayerEvent {
    private static final HandlerList handlers = new HandlerList();

    @Getter
    private Arena arena = null;

    public static HandlerList getHandlers() {
        return handlers;
    }

    public PlayerJoinSpectatorEvent(Player p, Arena a) {
        this.player = p;
        this.arena = a;
    }
}
