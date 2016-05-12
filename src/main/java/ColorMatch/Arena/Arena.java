package main.java.ColorMatch.Arena;

import cn.nukkit.Player;
import cn.nukkit.block.BlockAir;
import cn.nukkit.block.BlockWool;
import cn.nukkit.event.HandlerList;
import cn.nukkit.inventory.PlayerInventory;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemBlock;
import cn.nukkit.math.Vector3;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.TextFormat;
import lombok.Getter;
import main.java.ColorMatch.ColorMatch;
import cn.nukkit.event.Listener;
import main.java.ColorMatch.Utils.WoolColor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class Arena extends ArenaManager implements Listener {

    public static final int LOBBY = 0;
    public static final int GAME = 1;

    @Getter
    protected int phase = LOBBY;

    @Getter
    protected boolean enabled = false;

    @Getter
    protected HashMap<String, Player> players = new HashMap<>();

    @Getter
    protected HashMap<String, Player> spectators = new HashMap<>();

    protected ColorMatch plugin;
    protected Config config;
    protected ArenaListener listener;
    protected ArenaSchedule scheduler;

    @Getter
    protected String name = "";

    protected int currentColor = 0;

    protected ArrayList<Player> winners = new ArrayList<>();

    public Arena(ColorMatch plugin, String name, Config cfg) {
        this.name = name;
        super.plugin = this;
        this.listener = new ArenaListener(this);
        this.scheduler = new ArenaSchedule(this);
        this.config = cfg;
        this.plugin = plugin;
    }

    public boolean enable() {
        if (!this.init(config)) {
            return false;
        }

        this.plugin.getServer().getPluginManager().registerEvents(listener, plugin);
        scheduler.id = this.plugin.getServer().getScheduler().scheduleRepeatingTask(scheduler, 20).getTaskId();
        this.enabled = true;
        return true;
    }

    public boolean disable() {
        HandlerList.unregisterAll(listener);
        this.plugin.getServer().getScheduler().cancelTask(scheduler.getId());
        enabled = false;
        return true;
    }

    public void start() {
        start(false);
    }

    public void start(boolean force) {
        if (players.size() < plugin.conf.getMinPlayers() && !force) {
            return;
        }

        for (Player p : players.values()) {
            p.teleport(getStartPos());
            p.sendMessage(ColorMatch.getPrefix() + TextFormat.AQUA + "Game started!");
        }

        this.phase = GAME;
        updateJoinSign();
    }

    public void stop() {
        for (Player p : players.values()) {
            removeFromArena(p);
        }

        this.phase = LOBBY;
        updateJoinSign();
        resetFloor();
    }

    public void endGame() {
        if (this.players.size() == 1) {
            winners.add(this.players.values().toArray(new Player[1])[0]);
        }

        String message = "§6§l---------------------§r\n§5arena " + getName() + " ended\n§4winners:\n§a1. %2\n§e2. %3\n&c3. %4\n§6§l---------------------";

        int i = 2;

        for (Player p : winners) {
            String replace = "---";

            if (p != null) {
                replace = p.getDisplayName();
            }

            message = message.replaceAll("%" + i, replace);
        }

        stop();
    }

    public void addToArena(Player p) {
        if (phase == GAME) {
            this.addSpectator(p);
            return;
        }

        if (players.size() >= plugin.conf.getMaxPlayers() && !p.hasPermission("colormatch.joinfullarena")) {
            p.sendMessage(ColorMatch.getPrefix() + TextFormat.RED + "This game is full");
            return;
        }

        String msg = ColorMatch.getPrefix() + TextFormat.YELLOW + p.getDisplayName() + TextFormat.GRAY + " has joined (" + (TextFormat.BLUE + players.size() + 1) + TextFormat.YELLOW + "/" + TextFormat.BLUE + plugin.conf.getMaxPlayers();
        for (Player pl : players.values()) {
            pl.sendMessage(msg);
        }

        this.players.put(p.getName().toLowerCase(), p);
        updateJoinSign();

        p.teleport(getStartPos());
        p.sendMessage(ColorMatch.getPrefix() + TextFormat.GRAY + "Joining to arena " + TextFormat.YELLOW + this.name + TextFormat.GRAY + "...");

        checkLobby();
    }

    public void removeFromArena(Player p) {
        this.players.remove(p.getName().toLowerCase());
        updateJoinSign();
        resetPlayer(p);

        String msg = ColorMatch.getPrefix() + TextFormat.YELLOW + p.getDisplayName() + TextFormat.GRAY + " has left (" + (TextFormat.BLUE + players.size() + 1) + TextFormat.YELLOW + "/" + TextFormat.BLUE + plugin.conf.getMaxPlayers();
        for (Player pl : players.values()) {
            pl.sendMessage(msg);
        }

        if (p.isOnline()) {
            p.sendMessage(ColorMatch.getPrefix() + TextFormat.GRAY + "Leaving arena...");
        }

        p.teleport(plugin.conf.getLobby());

        if (phase == GAME) {
            if (this.players.size() <= 2) {
                this.winners.add(p);
            }

            checkAlive();
        }
    }

    public void addSpectator(Player p) {
        p.teleport(getSpectatorPos());
        p.sendMessage(ColorMatch.getPrefix() + TextFormat.GRAY + "Joining to as spectator...");
        this.spectators.put(p.getName().toLowerCase(), p);
    }

    public void removeSpectator(Player p) {
        this.spectators.remove(p.getName().toLowerCase());
        p.teleport(plugin.conf.getLobby());
    }

    public void selectNewColor() {
        this.currentColor = new Random().nextInt(15);

        Item item = new ItemBlock(new BlockWool(currentColor));

        for (Player p : players.values()) {
            PlayerInventory inv = p.getInventory();

            for (int i = 0; i < 9 && i < inv.getSize(); i++) {
                inv.setItem(i, item);
                inv.setHotbarSlotIndex(i, i);
            }

            String msg = WoolColor.getColorName(currentColor) + "\n\n\n\n";
            p.sendPopup(msg);
        }
    }

    public void resetFloor() {
        Random random = new Random();
        BlockWool block = new BlockWool();
        Vector3 v = new Vector3();

        int colorCount = 0;
        int blockCount = 0;

        int floorSize = (int) (((floor.maxX - floor.minX) / 3) * ((floor.maxZ - floor.minZ) / 3));

        int randomBlock = random.nextInt(floorSize);
        int maxColorCount = (int) floorSize / 16;

        for (int x = (int) getFloor().minX; x <= getFloor().maxX; x += 3) {
            for (int z = (int) getFloor().minZ; z <= getFloor().maxZ; z += 3) {
                int color = random.nextInt(15);

                int minX = x - 1;
                int minZ = z - 1;
                int maxX = x + 1;
                int maxZ = z + 1;

                if (color == this.currentColor) {
                    colorCount++;
                } else if (blockCount == randomBlock && colorCount < maxColorCount) {
                    color = this.currentColor;
                }

                block.setDamage(color);

                for (int x_ = minX; x_ <= maxX; x_++) {
                    for (int z_ = minZ; z_ <= maxZ; z_++) {
                        level.setBlock(v.setComponents(x_, floor.minY, z_), block.clone(), true, false);
                    }
                }

                blockCount++;
            }
        }
    }

    public void removeFloor() {
        Vector3 v = new Vector3();

        for (int x = (int) getFloor().minX; x <= getFloor().maxX; x++) {
            for (int z = (int) getFloor().minZ; z <= getFloor().maxZ; z++) {
                if (level.getBlockDataAt(x, (int) floor.minY, z) != currentColor) {
                    level.setBlock(v.setComponents(x, floorPos.getFloorY(), z), new BlockAir(), true, false);
                }
            }
        }
    }
}
