package com.creeperface.nukkitx.colormatch;

import cn.nukkit.AdventureSettings;
import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockIron;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.inventory.PlayerInventory;
import cn.nukkit.item.*;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.plugin.Plugin;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.TextFormat;
import com.creeperface.nukkitx.colormatch.arena.Arena;
import com.creeperface.nukkitx.colormatch.economy.EconomyAPIProvider;
import com.creeperface.nukkitx.colormatch.economy.EconomyProvider;
import com.creeperface.nukkitx.colormatch.economy.LeetEconomyProvider;
import com.creeperface.nukkitx.colormatch.eventhandler.MainListener;
import com.creeperface.nukkitx.colormatch.lang.BaseLang;
import com.creeperface.nukkitx.colormatch.stats.*;
import com.creeperface.nukkitx.colormatch.utils.ArenaBuilder;
import com.creeperface.nukkitx.colormatch.utils.Reward;
import com.creeperface.nukkitx.colormatch.utils.Utils;
import lombok.Getter;

import java.io.File;
import java.io.FilenameFilter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ColorMatch extends PluginBase {

    public MainConfiguration conf;

    @Getter
    private final Map<String, Arena> arenas = new HashMap<>();

    @Getter
    private final Map<String, Arena> setters = new HashMap<>();

    @Getter
    private BaseLang language = null;

    @Getter
    private EconomyProvider economy = null;

    @Getter
    private StatsProvider stats = null;

    @Getter
    private Reward reward = null;

    @Getter
    private static ColorMatch instance = null;

    @Override
    public void onLoad() {
        instance = this;
    }

    @Override
    public void onEnable() {
        new File(this.getDataFolder(), "arenas").mkdirs();
        boolean first = saveResource("config.yml");
        saveResource("lang/English.yml");

        this.conf = new MainConfiguration(this);
        if (!this.conf.load(first)) {
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }

        this.conf.save();

        initLanguage();
        initEconomy();
        initStats();

        reward = new Reward();
        reward.init(this, conf.getReward());

        this.getServer().getPluginManager().registerEvents(new MainListener(this), this);
        this.registerArenas();
    }

    public void onDisable() {
        getServer().getLogger().debug(getLanguage().translateString("general.disable_all"));

        arenas.forEach((name, arena) -> {
            getServer().getLogger().debug(getLanguage().translateString("general.disable_arena", name));
            arena.disable();
            arena.save(false);
        });

        stats.onDisable();
    }

    public static String getPrefix() {
        return "§9[§cCM§9] ";
    }

    public boolean registerArena(String name, File file) {
        Arena arena = new Arena(this, name, file);
        arenas.put(name, arena);
        return arena.enable();
    }

    private void registerArenas() {
        getServer().getLogger().info(getLanguage().translateString("general.load_all"));
        File arenas = new File(this.getDataFolder(), "arenas");

        if (!arenas.isDirectory()) {
            getServer().getLogger().info(getLanguage().translateString("general.no_arenas"));
            return;
        }

        File[] files = arenas.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.lastIndexOf('.') > 0 && name.substring(name.lastIndexOf('.')).equals(".yml");
            }
        });

        if (files == null || files.length == 0) {
            getServer().getLogger().info(getLanguage().translateString("general.no_arenas"));
            return;
        }

        for (File file : files) {
            String fileName = file.getName().toLowerCase().trim();
            String name = fileName.substring(0, fileName.length() - 4);

            if (registerArena(name, file)) {
                getServer().getLogger().info(getLanguage().translateString("general.load_arena", name));
            } /*else {
                this.getLogger().info(TextFormat.GRAY + file.getName()+TextFormat.RED+" load failed");
            }*/
        }
    }

    public Arena getArena(String name) {
        return arenas.get(name.toLowerCase());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().toLowerCase().equals("colormatch")) {
            if (args.length < 1/* && !args[0].toLowerCase().equals("help")*/) {
                sender.sendMessage(getHelp(sender, 0));
                return true;
            }

            Arena arena = args.length >= 2 ? arenas.get(args[1].toLowerCase()) : null;

            if (!sender.hasPermission("cm.command." + args[0].toLowerCase())) {
                sender.sendMessage(cmd.getPermissionMessage());
                return true;
            }

            switch (args[0].toLowerCase()) {
                case "unload":
                case "disable":
                    if (args.length != 2) {
                        sender.sendMessage(getLanguage().translateString("commands.help.disable"));
                        return true;
                    }

                    if (arena == null) {
                        sender.sendMessage(getLanguage().translateString("general.arena_doesnt_exist2", args[1]));
                        return true;
                    }

                    if (!arena.isEnabled()) {
                        sender.sendMessage(getLanguage().translateString("general.arena_already_disabled"));
                        return true;
                    }

                    sender.sendMessage(getLanguage().translateString("commands.success.disable", arena.getName()));
                    arena.disable();
                    break;
                case "load":
                case "enable":
                    if (args.length != 2) {
                        sender.sendMessage(getLanguage().translateString("commands.help.enable"));
                        return true;
                    }

                    if (arena == null) {
                        sender.sendMessage(getLanguage().translateString("general.arena_doesnt_exist2", args[1]));
                        return true;
                    }

                    if (arena.isEnabled()) {
                        sender.sendMessage(getLanguage().translateString("general.arena_already_enabled"));
                        return true;
                    }

                    if (!arena.enable()) {
                        sender.sendMessage(getLanguage().translateString("commands.failure.enable"));

                        List<String> fields = arena.checkConfiguration();
                        if (!fields.isEmpty()) {
                            sender.sendMessage(getPrefix() + TextFormat.RED + "Following arena properties aren't configured properly: " + TextFormat.YELLOW + String.join(", ", fields));
                        }
                        return true;
                    }

                    sender.sendMessage(getLanguage().translateString("commands.success.enable", arena.getName()));
                    break;
                case "start":
                    if (!(sender instanceof Player) && args.length != 2) {
                        sender.sendMessage(getLanguage().translateString("commands.help.start"));
                        return true;
                    }

                    if (arena == null) {
                        if (sender instanceof Player) {
                            arena = getPlayerArena((Player) sender);
                        }

                        if (arena == null) {
                            sender.sendMessage(getLanguage().translateString("general.arena_doesnt_exist2", args[1]));
                            return true;
                        }
                    }

                    arena.start(true);
                    sender.sendMessage(getLanguage().translateString("commands.success.start"));
                    break;
                case "stop":
                    if (!(sender instanceof Player) && args.length != 2) {
                        sender.sendMessage(getLanguage().translateString("commands.help.stop"));
                        return true;
                    }

                    if (arena == null) {
                        if (sender instanceof Player) {
                            arena = getPlayerArena((Player) sender);
                        }

                        if (arena == null) {
                            sender.sendMessage(getLanguage().translateString("general.arena_doesnt_exist2", args[1]));
                            return true;
                        }
                    }

                    arena.stop();
                    sender.sendMessage(getLanguage().translateString("commands.success.stop"));
                    break;
                case "join":
                    if (!(sender instanceof Player)) {
                        sender.sendMessage(getLanguage().translateString("commands.failure.run_command_ingame"));
                        return true;
                    }

                    if (args.length != 2) {
                        sender.sendMessage(getLanguage().translateString("commands.help.join"));
                        return true;
                    }

                    if (arena == null) {
                        arena = getPlayerArena((Player) sender);

                        if (arena == null) {
                            sender.sendMessage(getLanguage().translateString("general.arena_doesnt_exist2", args[1]));
                            return true;
                        }
                    }

                    arena.addToArena((Player) sender);
                    break;
                case "leave":
                    if (!(sender instanceof Player)) {
                        sender.sendMessage(getLanguage().translateString("commands.failure.run_command_ingame"));
                        return true;
                    }

                    if (arena == null) {
                        arena = getPlayerArena((Player) sender);

                        if (arena == null) {
                            sender.sendMessage(getLanguage().translateString("general.arena_doesnt_exist2", args[1]));
                            return true;
                        }
                    }

                    if (arena.isSpectator((Player) sender)) {
                        arena.removeSpectator((Player) sender);
                    } else {
                        arena.removeFromArena((Player) sender);
                    }
                    break;
                case "configure":
                    if (!(sender instanceof Player)) {
                        sender.sendMessage(getLanguage().translateString("commands.failure.run_command_ingame"));
                        return true;
                    }

                    if (args.length != 2) {
                        sender.sendMessage(getLanguage().translateString("commands.help.configure"));
                        return true;
                    }

                    if (arena == null) {
                        sender.sendMessage(getLanguage().translateString("general.arena_doesnt_exist2", args[1]));
                        return true;
                    }

                    if (arena.getPhase() == Arena.PHASE_GAME) {
                        sender.sendMessage(getLanguage().translateString("general.arena_running"));
                        return true;
                    }

                    arena.disable();
                    arena.setup = true;
                    setters.put(sender.getName(), arena);
                    giveSetupTools((Player) sender);
                    ((Player) sender).getAdventureSettings().set(AdventureSettings.Type.ALLOW_FLIGHT, true);
                    sender.sendMessage(getLanguage().translateString("setupmode.enable", arena.getName()));
                    break;
                case "create":
                    if (args.length != 2) {
                        sender.sendMessage(getLanguage().translateString("commands.help.create"));
                        return true;
                    }

                    if (arena != null) {
                        sender.sendMessage(getLanguage().translateString("general.arena_doesnt_exist2", args[1]));
                        break;
                    }

                    arena = new Arena(this, args[1].toLowerCase(), new File(getDataFolder() + "/arenas/" + args[1].toLowerCase() + ".yml"));

                    arenas.put(args[1].toLowerCase(), arena);

                    sender.sendMessage(getLanguage().translateString("commands.success.create", arena.getName()));
                    break;
                case "remove":
                    break;
                case "help":
                    sender.sendMessage(getHelp(sender, 0));
                    break;
                case "setlobby":
                    if (args.length < 4) {
                        if (!(sender instanceof Player)) {
                            sender.sendMessage(getLanguage().translateString("commands.help.setlobby"));
                            break;
                        } else {
                            this.conf.setMainLobby(((Player) sender).clone());
                            sender.sendMessage(language.translateString("commands.success.setlobby"));
                        }
                    } else {
                        int x;
                        int y;
                        int z;

                        try {
                            x = Integer.valueOf(args[1]);
                            y = Integer.valueOf(args[2]);
                            z = Integer.valueOf(args[3]);
                        } catch (NumberFormatException e) {
                            sender.sendMessage(getLanguage().translateString("commands.help.setlobby_player"));
                            break;
                        }

                        Level level = null;

                        if (args.length > 4) {
                            String world = args[4];

                            level = getServer().getLevelByName(world);

                            if (level == null) {
                                if (!getServer().loadLevel(world)) {
                                    sender.sendMessage(getLanguage().translateString("commands.failure.unknown_world", world));
                                    break;
                                }

                                level = getServer().getLevelByName(world);
                            }
                        } else if (sender instanceof Player) {
                            level = ((Player) sender).getLevel();
                        }

                        conf.setMainLobby(new Position(x, y, z, level != null ? level : getServer().getDefaultLevel()));
                        sender.sendMessage(language.translateString("commands.success.setlobby"));
                    }
                    break;
                case "build":
                    if (args.length != 2 && args.length != 3) {
                        sender.sendMessage(getLanguage().translateString("commands.help.build"));
                        return true;
                    }

                    Block b = new BlockIron();

                    if (args.length == 3) {
                        Block b2 = Utils.fromString(args[2]);

                        if (b2.getId() == 0) {
                            sender.sendMessage(getLanguage().translateString("commands.failure.invalid_block", args[2]));
                            return true;
                        } else {
                            b = b2;
                        }
                    }

                    if (arena == null) {
                        sender.sendMessage(getLanguage().translateString("general.arena_doesnt_exist2", args[1]));
                        return true;
                    }

                    if (!arena.isEnabled()) {
                        sender.sendMessage(language.translateString("commans.failure.build_enable"));
                        return true;
                    }

                    sender.sendMessage(getLanguage().translateString("commands.success.build", arena.getName()));

                    sender.sendMessage(ArenaBuilder.build(arena, arena.getLevel(), b));
                    break;
                case "list":
                    Level level = null;

                    if (args.length > 2) {
                        sender.sendMessage(language.translateString("commands.help.list"));
                        return true;
                    }

                    if (args.length == 2) {
                        level = getServer().getLevelByName(args[1]);

                        if (level == null) {
                            sender.sendMessage(language.translateString("commands.failure.unknown_world", args[1]));
                            return true;
                        }
                    }

                    final Level lvl = level;

                    List<Arena> arenas = getArenas().values().stream().filter((a) -> lvl == null || a.getLevel() == lvl).collect(Collectors.toList());

                    if (arenas.isEmpty()) {
                        sender.sendMessage(language.translateString("commands.failure.list_empty"));
                        return true;
                    }

                    StringBuilder builder = new StringBuilder(getLanguage().translateString("general.available_arenas"));
                    arenas.forEach((a) -> {
                        builder.append('\n');
                        builder.append(TextFormat.GRAY);
                        builder.append("- ");
                        builder.append(a.isEnabled() ? TextFormat.GREEN : TextFormat.RED);
                        builder.append(a.getName());
                    });

                    sender.sendMessage(builder.toString());
                    break;
                case "stats":
                    if (!sender.isPlayer() && args.length < 2) {
                        sender.sendMessage(language.translateString("commands.failure.run_command_ingame"));
                        return true;
                    }

                    if (args.length > 2) {
                        sender.sendMessage(language.translateString("commands.help.stats"));
                        return true;
                    }

                    getStats().sendStats(args.length == 2 ? args[1].toLowerCase() : sender.getName().toLowerCase(), sender);
                    break;
                default:
                    sender.sendMessage(getHelp(sender, 1));
                    break;
            }
        }

        return true;
    }

    private String getHelp(CommandSender sender, int page) {
        String help = getLanguage().translateString("help.main", "1", "1");

        String arena = getLanguage().translateString("custom_words.arena", false);

        if (sender.hasPermission("colormatch.command.start"))
            help += "\n" + TextFormat.YELLOW + "/cm start [" + arena + "]";
        if (sender.hasPermission("colormatch.command.stop"))
            help += "\n" + TextFormat.YELLOW + "/cm stop [" + arena + "]";
        if (sender.hasPermission("colormatch.command.join"))
            help += "\n" + TextFormat.YELLOW + "/cm join <" + arena + ">";
        if (sender.hasPermission("colormatch.command.leave")) help += "\n" + TextFormat.YELLOW + "/cm leave";
        if (sender.hasPermission("colormatch.command.enable"))
            help += "\n" + TextFormat.YELLOW + "/cm enable <" + arena + ">";
        if (sender.hasPermission("colormatch.command.disable"))
            help += "\n" + TextFormat.YELLOW + "/cm disable <" + arena + ">";
        if (sender.hasPermission("colormatch.command.configure"))
            help += "\n" + TextFormat.YELLOW + "/cm configure <" + arena + ">";
        if (sender.hasPermission("colormatch.command.create"))
            help += "\n" + TextFormat.YELLOW + "/cm create <" + arena + ">";
        if (sender.hasPermission("colormatch.command.list"))
            help += "\n" + TextFormat.YELLOW + "/cm list [" + getLanguage().translateString("custom_words.world", false) + "]";
        if (sender.hasPermission("colormatch.command.remove"))
            help += "\n" + TextFormat.YELLOW + "/cm remove <" + arena + ">";
        if (sender.hasPermission("colormatch.command.build"))
            help += "\n" + TextFormat.YELLOW + "/cm build <" + arena + ">";
        if (sender.hasPermission("colormatch.command.stats"))
            help += "\n" + TextFormat.YELLOW + "/cm stats [" + getLanguage().translateString("custom_words.player", false) + "]";
        if (sender.hasPermission("colormatch.command.help")) help += "\n" + TextFormat.YELLOW + "/cm help";

        return help;
    }

    public Arena getPlayerArena(Player p) {
        for (Arena a : arenas.values()) {
            if (a.isSpectator(p) || a.inArena(p)) {
                return a;
            }
        }

        return null;
    }

    private void giveSetupTools(Player p) {
        PlayerInventory inv = p.getInventory();
        inv.clearAll();

        Item[] items = new Item[4];

        items[0] = new ItemShovelGold().setCustomName("" + TextFormat.RESET + TextFormat.GREEN + "Start position");
        items[1] = new ItemPickaxeGold().setCustomName("" + TextFormat.RESET + TextFormat.GREEN + "Floor position");
        items[2] = new ItemAxeGold().setCustomName("" + TextFormat.RESET + TextFormat.GREEN + "Spectator spawn");
        items[3] = new ItemHoeGold().setCustomName("" + TextFormat.RESET + TextFormat.GREEN + "Join sign");

        /*inv.setItem(0, new ItemSwordGold().setCustomName(TextFormat.RESET + TextFormat.GREEN + "Start position"));
        inv.setItem(1, new ItemPickaxeGold().setCustomName(TextFormat.RESET + TextFormat.GREEN + "Floor position"));
        inv.setItem(2, new ItemAxeGold().setCustomName(TextFormat.RESET + TextFormat.GREEN + "Spectator spawn"));
        inv.setItem(3, new ItemHoeGold().setCustomName(TextFormat.RESET + TextFormat.GREEN + "Join sign"));*/

        for (int i = 0; i < 4; i++) {
            inv.setItem(i, items[i]);
            inv.setHeldItemIndex(i);
        }

        inv.sendContents(p);
    }

    private void initLanguage() {
        File languages = new File(getDataFolder(), "lang");
        String lang = conf.getLanguage();

        if (!languages.exists() || !languages.isDirectory()) {
            getLogger().error("Could not load default language");
            return;
        } else {
            File[] files = languages.listFiles((dir, name) -> name.endsWith(".yml"));

            if (files == null) {
                getLogger().error("Could not load default language");
                return;
            }

            File langFile = null;

            for (File l : files) {
                if (l.getName().equalsIgnoreCase(lang + ".yml")) {
                    langFile = l;
                    break;
                }
            }

            if (langFile == null) {
                getLogger().warning("Could not find language file '" + lang + ".yml'. Selecting English as default language");
                langFile = new File(getDataFolder() + "/lang/English.yml");
            }

            BaseLang baseLang = new BaseLang();
            baseLang.init(langFile.getPath());

            language = baseLang;
        }
    }

    private void initEconomy() {
        Plugin plugin = getServer().getPluginManager().getPlugin("EconomyAPI");

        if (plugin != null) {
            economy = new EconomyAPIProvider(plugin);
        } else if ((plugin = getServer().getPluginManager().getPlugin("economy-LEET")) != null) {
            economy = new LeetEconomyProvider(plugin);
        }
    }

    private void initStats() {
        String stats = conf.getStats();

        switch (stats.toLowerCase()) {
            case "yml":
            case "yaml":
                this.stats = new YamlStatsProvider();
                break;
            case "json":
                //this.stats = new JsonStatsProvider();
                break;
            case "sqlite":
                this.stats = new SQLiteStatsProvider();
                break;
            case "mysql":
                this.stats = new MySQLStatsProvider();
                break;
            default:
                this.stats = new NoneProvider();
        }

        if (!this.stats.init(this)) {
            getServer().getLogger().warning("Could not load selected stats provider");
        }
    }
}
