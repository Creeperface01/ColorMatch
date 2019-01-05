package com.creeperface.nukkitx.colormatch.event;

import cn.nukkit.Player;
import cn.nukkit.event.Event;
import cn.nukkit.event.HandlerList;
import com.creeperface.nukkitx.colormatch.arena.Arena;
import com.creeperface.nukkitx.colormatch.utils.Reward;
import lombok.Getter;

import java.util.ArrayDeque;

public class GameEndEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlers() {
        return handlers;
    }

    @Getter
    private Arena arena;

    @Getter
    private Reward reward;

    @Getter
    private Player winner;

    @Getter
    private ArrayDeque<Arena.WinnerEntry> players;

    public GameEndEvent(ArrayDeque<Arena.WinnerEntry> winners, Arena a, Reward reward) {
        this.winner = winners.getLast().player;
        this.players = new ArrayDeque<>(winners);
        this.arena = a;
        this.reward = reward;
    }
}
