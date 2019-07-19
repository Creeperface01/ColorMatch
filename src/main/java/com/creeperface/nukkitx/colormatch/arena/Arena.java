package com.creeperface.nukkitx.colormatch.arena;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockAir;
import cn.nukkit.event.HandlerList;
import cn.nukkit.event.Listener;
import cn.nukkit.inventory.PlayerInventory;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemBlock;
import cn.nukkit.level.Position;
import cn.nukkit.math.Vector3;
import cn.nukkit.potion.Effect;
import cn.nukkit.scheduler.TaskHandler;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.DyeColor;
import cn.nukkit.utils.TextFormat;
import com.creeperface.nukkitx.colormatch.ColorMatch;
import com.creeperface.nukkitx.colormatch.event.GameEndEvent;
import com.creeperface.nukkitx.colormatch.event.PlayerJoinArenaEvent;
import com.creeperface.nukkitx.colormatch.event.PlayerQuitArenaEvent;
import com.creeperface.nukkitx.colormatch.utils.BossBar;
import com.creeperface.nukkitx.colormatch.utils.Reward;
import com.google.common.collect.Iterables;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.util.*;

public class Arena extends ArenaManager implements Listener {

    public static final int PHASE_LOBBY = 0;
    public static final int PHASE_GAME = 1;

    @Getter
    protected int phase = PHASE_LOBBY;

    @Getter
    protected boolean enabled = false;

    public boolean setup = false;

    @Getter
    protected Map<String, Player> players = new HashMap<>();

    @Getter
    protected Map<String, Player> spectators = new HashMap<>();

    protected Map<String, SavedPlayer> saves = new HashMap<>();

    protected ColorMatch plugin;
    protected ArenaListener listener;
    protected ArenaSchedule scheduler;

    @Getter
    protected String name = "";

    protected int currentColor = 0;

    @Getter
    private int round = -1;

    public boolean starting = false;

    @Getter
    private long startTime;

    protected ArrayDeque<WinnerEntry> winners = new ArrayDeque<>();

    protected BossBar bossBar;

    protected TaskHandler fastTask;

    public Arena(ColorMatch plugin, String name, File cfg) {
        load(cfg.getPath(), Config.YAML);
        this.name = name;
        super.plugin = this;
        this.listener = new ArenaListener(this);
        this.scheduler = new ArenaSchedule(this);
        this.bossBar = new BossBar(plugin);
        this.plugin = plugin;
    }

    public boolean enable() {
        if (enabled || setup || !this.init()) {
            return false;
        }

        this.plugin.getServer().getPluginManager().registerEvents(listener, plugin);
        scheduler = new ArenaSchedule(this);
        scheduler.id = this.plugin.getServer().getScheduler().scheduleRepeatingTask(plugin, scheduler, 20).getTaskId();
        this.enabled = true;

        this.bossBar.setMaxHealth(this.colorChangeInterval * 10);
        this.bossBar.setHealth(this.colorChangeInterval * 10);

        this.bossBar.updateText(plugin.getLanguage().translateString("waiting_players", false, "0", plugin.conf.getMaxPlayers() + ""));

        updateJoinSign();
        resetFloor();
        return true;
    }

    public boolean disable() {
        if (!enabled) {
            return false;
        }

        if (phase == PHASE_GAME) {
            endGame();
        } else {
            stop();
        }

        HandlerList.unregisterAll(listener);
        this.plugin.getServer().getScheduler().cancelTask(scheduler.getId());
        enabled = false;
        starting = false;
        updateJoinSign();
        return true;
    }

    public void start() {
        start(false);
    }

    public void start(boolean force) {
        starting = false;

        if (players.size() < plugin.conf.getMinPlayers() && !force) {
            return;
        }

        Effect effect = getGameEffect();

        Position startPos = getStartPos().add(0.5, 0, 0.5);
        players.values().forEach(p -> {
            p.teleport(startPos);
            p.sendMessage(plugin.getLanguage().translateString("game.start_game"));

            if (effect != null) {
                p.addEffect(effect.clone());
            }

            p.extinguish();
        });

        this.round = -1;
        scheduler.colorTime = 0;
        scheduler.floor = true;
        scheduler.floorResetedTick = plugin.getServer().getTick();

        this.fastTask = this.plugin.getServer().getScheduler().scheduleRepeatingTask(plugin, new FastTask(this), 2);

        startTime = System.currentTimeMillis();

        this.phase = PHASE_GAME;
        updateJoinSign();
        selectNewColor();

        this.bossBar.updateData();
    }

    public void stop() {
        this.phase = PHASE_LOBBY;

        new ArrayList<>(players.values()).forEach(p -> removeFromArena(p, false));

        for (Player p : new ArrayList<>(spectators.values())) {
            removeSpectator(p);
        }

        this.round = -1;
        scheduler.time = 0;
        scheduler.colorTime = 0;

        if (this.fastTask != null) {
            fastTask.cancel();
        }

        updateJoinSign();
        resetFloor();
        winners.clear();

        this.bossBar.updateText(plugin.getLanguage().translateString("waiting_players", false, "0", plugin.conf.getMaxPlayers() + ""));
    }

    public void endGame() {
        if (this.players.size() == 1) {
            winners.add(new WinnerEntry(Iterables.getOnlyElement(this.players.values())));
        }

        if (!winners.isEmpty()) {
            WinnerEntry winner = winners.getLast();
            Reward reward = plugin.getReward();

            plugin.getStats().updateStats(winner.player.getName(), true, getRound());

            GameEndEvent ev = new GameEndEvent(winners, this, reward);
            plugin.getServer().getPluginManager().callEvent(ev);

            reward.give(winner.player);
        }

        String message = plugin.getLanguage().translateString("game.end_game", false, getName());

        for (int i = 1; i < 4; i++) {
            String replace = "---";

            if (!winners.isEmpty()) {
                WinnerEntry winner = winners.removeLast();

                long timeDiff = (winner.time - startTime) / 1000;
                replace = winner.player.getDisplayName() + TextFormat.GRAY + "(" + String.format("%02d:%02d", timeDiff / 60, (timeDiff % 60)) + ")";
            }

            message = message.replaceAll("%" + i, replace);
        }

        plugin.getServer().broadcastMessage(message);

        stop();
    }

    public void addToArena(Player p) {
        if (plugin.getPlayerArena(p) != null) {
            p.sendMessage(plugin.getLanguage().translateString("game.already"));
            return;
        }

        if (setup) {
            p.sendMessage(plugin.getLanguage().translateString("general.arena_in_setup_mode"));
            return;
        }

        if (phase == PHASE_GAME) {
            SavedPlayer save = new SavedPlayer();
            save.save(p);
            saves.put(p.getName().toLowerCase(), save);

            this.addSpectator(p);
            return;
        }

        if (players.size() >= plugin.conf.getMaxPlayers() && !p.hasPermission("colormatch.joinfullarena")) {
            p.sendMessage(plugin.getLanguage().translateString("general.game_full"));
            return;
        }

        PlayerJoinArenaEvent e = new PlayerJoinArenaEvent(p, this);
        plugin.getServer().getPluginManager().callEvent(e);

        if (e.isCancelled()) {
            return;
        }

        messageArenaPlayers(plugin.getLanguage().translateString("game.join_others", p.getDisplayName(), String.valueOf(players.size() + 1), String.valueOf(plugin.conf.getMaxPlayers())));

        resetPlayer(p);
        this.players.put(p.getName().toLowerCase(), p);
        updateJoinSign();

        p.teleport(getStartPos().add(0.5, 0, 0.5));

        SavedPlayer save = new SavedPlayer();
        save.save(p);
        saves.put(p.getName().toLowerCase(), save);

        this.bossBar.updateText(plugin.getLanguage().translateString("general.waiting_players", false, this.players.size() + "", plugin.conf.getMaxPlayers() + ""));
        this.bossBar.updateData();

        this.bossBar.addPlayer(p);

        p.setGamemode(0);
        p.sendMessage(plugin.getLanguage().translateString("game.join", getName()));

        //p.setDisplayName(TextFormat.GRAY + "[" + TextFormat.GREEN + "GAME" + TextFormat.GRAY + "] " + TextFormat.WHITE + TextFormat.RESET + " " + p.getDisplayName());

        checkLobby();
    }

    public void onDeath(Player p) {
        resetPlayer(p);

        messageArenaPlayers(plugin.getLanguage().translateString("game.death", p.getDisplayName(), String.valueOf(players.size() - 1)));
        players.remove(p.getName().toLowerCase());

        if (this.players.size() <= 2) {
            this.winners.add(new WinnerEntry(p));
        }

        if (players.size() > 0) {
            plugin.getStats().updateStats(p.getName(), false, getRound());
        }

        addSpectator(p);
        checkAlive();
    }

    public void removeFromArena(Player p) {
        removeFromArena(p, true);
    }

    public void removeFromArena(Player p, boolean message) {
        this.players.remove(p.getName().toLowerCase());

        PlayerQuitArenaEvent ev = new PlayerQuitArenaEvent(p, this);
        plugin.getServer().getPluginManager().callEvent(ev);

        updateJoinSign();

        if (message) {
            messageArenaPlayers(plugin.getLanguage().translateString("game.leave_others", p.getDisplayName(), String.valueOf(players.size() - 1), String.valueOf(plugin.conf.getMaxPlayers())));

            if (p.isOnline()) {
                p.sendMessage(plugin.getLanguage().translateString("game.leave"));
            }
        }

        p.teleport(plugin.conf.getMainLobby().getLevel().getSafeSpawn(plugin.conf.getMainLobby()));

        resetPlayer(p);
        this.bossBar.removePlayer(p);

        this.bossBar.updateText(plugin.getLanguage().translateString("general.waiting_players", false, this.players.size() + "", plugin.conf.getMaxPlayers() + ""));
        this.bossBar.updateData();

        SavedPlayer save = saves.remove(p.getName().toLowerCase());

        if (save != null) {
            save.load(p);
        }

        if (phase == PHASE_GAME) {
            if (this.players.size() <= 2) {
                this.winners.add(new WinnerEntry(p));
            }

            checkAlive();
        }
    }

    public void addSpectator(Player p) {
        resetPlayer(p);
        p.teleport(getSpectatorPos().add(0.5, 0, 0.5));
        p.sendMessage(plugin.getLanguage().translateString("game.join_spectator"));
        p.setGamemode(0);
        bossBar.addPlayer(p);
        bossBar.updateData();
        //p.setDisplayName(TextFormat.GRAY + "[" + TextFormat.YELLOW + "SPECTATOR" + TextFormat.GRAY + "] " + TextFormat.WHITE + TextFormat.RESET + " " + p.getDisplayName());
        this.spectators.put(p.getName().toLowerCase(), p);
    }

    public void removeSpectator(Player p) {
        //p.setDisplayName(p.getDisplayName().replaceAll(TextFormat.GRAY + "[" + TextFormat.YELLOW + "SPECTATOR" + TextFormat.GRAY + "] " + TextFormat.WHITE + TextFormat.RESET + " ", ""));
        this.spectators.remove(p.getName().toLowerCase());
        resetPlayer(p);
        this.bossBar.removePlayer(p);
        p.teleport(plugin.conf.getMainLobby().getLevel().getSafeSpawn(plugin.conf.getMainLobby()));

        SavedPlayer save = saves.remove(p.getName().toLowerCase());

        if (save != null) {
            save.load(p);
        }
    }

    public void selectNewColor() {
        this.currentColor = new Random().nextInt(15);

        Item item = new ItemBlock(getFloorMaterial(), currentColor);
        item.setDamage(currentColor);

        players.values().forEach((Player p) -> {
            PlayerInventory inv = p.getInventory();

            for (int i = 0; i < 9 && i < inv.getSize(); i++) {
                inv.setItem(i, item);
                inv.setHeldItemSlot(i);
            }

            inv.sendContents(p);

            inv.setItemInHand(item.clone());
            inv.sendHeldItem(p);

            /*String msg = WoolColor.getColorName(currentColor) + "\n\n\n\n";
            p.sendPopup(msg);*/
        });

        TextFormat chatColor = TextFormat.values()[new Random().nextInt(16)];

        this.bossBar.setHealth(colorChangeInterval * 10);
        this.bossBar.updateText(chatColor + DyeColor.getByWoolData(currentColor).getName());
        this.bossBar.updateData();
    }

    public void resetFloor() {
        Random random = new Random();
        Block block = getFloorMaterial();
        Vector3 v = new Vector3();

        int colorCount = 0;
        int blockCount = 0;

        int floorSize = (int) (((floor.getMaxX() - floor.getMinX() + 1) / 3) * ((floor.getMaxZ() - floor.getMinZ() + 1) / 3));

        int randomBlock = random.nextInt(floorSize);
        int maxColorCount = floorSize / 25;

        //System.out.println("floor size: "+floorSize+"        max count: "+maxColorCount);

        for (int x = (int) getFloor().getMinX() + 1; x <= getFloor().getMaxX() - 1; x += 3) {
            for (int z = (int) getFloor().getMinZ() + 1; z <= getFloor().getMaxZ() - 1; z += 3) {
                int color = random.nextInt(15);

                int minX = x - 1;
                int minZ = z - 1;
                int maxX = x + 1;
                int maxZ = z + 1;

                if (color == this.currentColor) {
                    if (colorCount >= maxColorCount) {
                        color += color < 15 ? 1 : -1;
                    } else {
                        colorCount++;
                    }
                } else if (blockCount == randomBlock && colorCount < maxColorCount / 2) {
                    color = this.currentColor;
                }

                block.setDamage(color);

                for (int x_ = minX; x_ <= maxX; x_++) {
                    for (int z_ = minZ; z_ <= maxZ; z_++) {
                        level.setBlock(v.setComponents(x_, floorPos.getFloorY(), z_), block.clone(), false, false);
                    }
                }

                blockCount++;
            }
        }

        if(this.phase > PHASE_LOBBY) {
            round++;
        }
    }

    public void removeFloor() {
        Vector3 v = new Vector3();
        BlockAir air = new BlockAir();

        /*for (int x = (int) getFloor().minX; x <= getFloor().maxX; x++) {
            for (int z = (int) getFloor().minZ; z <= getFloor().maxZ; z++) {
                int meta = level.getBlockDataAt(x, floorPos.getFloorY(), z);

                //int meta = level.getChunk(x >> 4, z >> 4, true).getBlockData(x & 15, floorPos.getFloorY() & 127, z & 15);

                if (meta != currentColor) {
                    level.setBlock(v.setComponents(x, floorPos.getFloorY(), z), new BlockAir(), true, false);
                }
            }
        }*/

        for (int x = (int) getFloor().getMinX() + 1; x <= getFloor().getMaxX() - 1; x += 3) {
            for (int z = (int) getFloor().getMinZ() + 1; z <= getFloor().getMaxZ() - 1; z += 3) {
                int minX = x - 1;
                int minZ = z - 1;
                int maxX = x + 1;
                int maxZ = z + 1;

                int meta = level.getBlockDataAt(x, floorPos.getFloorY(), z);

                //int meta = level.getChunk(x >> 4, z >> 4, true).getBlockData(x & 15, floorPos.getFloorY() & 127, z & 15);

                if (meta != currentColor) {
                    for (int x_ = minX; x_ <= maxX; x_++) {
                        for (int z_ = minZ; z_ <= maxZ; z_++) {
                            level.setBlock(v.setComponents(x_, floorPos.getFloorY(), z_), air.clone(), false, false);
                        }
                    }
                }
            }
        }
    }

    @RequiredArgsConstructor
    public static class WinnerEntry {

        public final long time = System.currentTimeMillis();
        public final Player player;

    }
}
