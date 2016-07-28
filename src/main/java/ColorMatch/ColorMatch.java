package ColorMatch;

import ColorMatch.Economy.EconomyAPIProvider;
import ColorMatch.Economy.EconomyProvider;
import ColorMatch.Economy.LeetEconomyProvider;
import ColorMatch.EventHandler.MainListener;
import ColorMatch.Lang.BaseLang;
import ColorMatch.Stats.MySQLStatsProvider;
import ColorMatch.Stats.NoneProvider;
import ColorMatch.Stats.StatsProvider;
import ColorMatch.Stats.YamlStatsProvider;
import ColorMatch.Utils.ArenaBuilder;
import ColorMatch.Utils.Reward;
import ColorMatch.Utils.Utils;
import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockIron;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.inventory.PlayerInventory;
import cn.nukkit.item.ItemAxeGold;
import cn.nukkit.item.ItemHoeGold;
import cn.nukkit.item.ItemPickaxeGold;
import cn.nukkit.item.ItemSwordGold;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.plugin.Plugin;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.TextFormat;
import lombok.Getter;
import ColorMatch.Arena.Arena;

import java.io.File;
import java.io.FilenameFilter;
import java.util.HashMap;
import java.util.Map;

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
    public void onLoad(){
        instance = this;
    }

    @Override
    public void onEnable() {
        new File(this.getDataFolder(), "arenas").mkdirs();
        boolean first = saveResource("config.yml");
        saveResource("lang");

        this.conf = new MainConfiguration();
        if (!this.conf.init(getDataFolder()+"/config.yml", first)) {
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }

        initLanguage();
        initEconomy();
        initStats();

        reward = new Reward();
        reward.init(this, conf.getSection("reward"));

        this.getServer().getPluginManager().registerEvents(new MainListener(this), this);
        this.registerArenas();
    }

    public void onDisable() {
        getLogger().debug("disabling all arenas...");

        arenas.forEach((name, arena) -> {
            getLogger().debug("disabling " + name + "...");
            arena.disable();
            arena.save(false);
        });
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
        this.getLogger().info(TextFormat.GREEN + "Loading arenas...");
        File arenas = new File(this.getDataFolder(), "arenas");

        if (!arenas.isDirectory()) {
            this.getLogger().info(TextFormat.GREEN + "No arenas found");
            return;
        }

        File[] files = arenas.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.lastIndexOf('.') > 0 && name.substring(name.lastIndexOf('.')).equals(".yml");
            }
        });

        if (files == null || files.length == 0) {
            this.getLogger().info(TextFormat.GREEN + "No arenas found");
            return;
        }

        for (File file : files) {
            String fileName = file.getName().toLowerCase().trim();
            String name = fileName.substring(0, fileName.length() - 4);

            if (registerArena(name, file)) {
                this.getLogger().info(TextFormat.GRAY + name + TextFormat.GREEN + " - load successful");
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
                        sender.sendMessage(ColorMatch.getPrefix() + TextFormat.GRAY + "Use " + TextFormat.YELLOW + " /cm disable <arena>");
                        return true;
                    }

                    if (arena == null) {
                        sender.sendMessage(getPrefix() + TextFormat.RED + "Arena '" + args[1] + "' doesn't exist");
                        return true;
                    }

                    if (!arena.isEnabled()) {
                        sender.sendMessage(getPrefix() + TextFormat.YELLOW + "Arena is already disabled");
                        return true;
                    }

                    sender.sendMessage(getPrefix() + TextFormat.GREEN + "Disabling arena " + TextFormat.YELLOW + arena.getName() + TextFormat.GREEN + "...");
                    boolean disabled = arena.disable();
                    break;
                case "load":
                case "enable":
                    if (args.length != 2) {
                        sender.sendMessage(ColorMatch.getPrefix() + TextFormat.GRAY + "Use " + TextFormat.YELLOW + " /cm enable <arena>");
                        return true;
                    }

                    if (arena == null) {
                        sender.sendMessage(getPrefix() + TextFormat.RED + "Arena '" + args[1] + "' doesn't exist");
                        return true;
                    }

                    if (arena.isEnabled()) {
                        sender.sendMessage(getPrefix() + TextFormat.YELLOW + "Arena is already enabled");
                        return true;
                    }

                    sender.sendMessage(getPrefix() + TextFormat.GREEN + "Enabling arena " + TextFormat.YELLOW + arena.getName() + TextFormat.GREEN + "...");

                    if (!arena.enable()) {
                        sender.sendMessage(getPrefix() + TextFormat.RED + "An error occurred during enabling");
                        return true;
                    }
                    break;
                case "start":
                    if (((sender instanceof Player) && args.length < 1) || (!(sender instanceof Player) && args.length != 2)) {
                        sender.sendMessage(ColorMatch.getPrefix() + TextFormat.GRAY + "Use " + TextFormat.YELLOW + " /cm start [arena]");
                        return true;
                    }

                    if (arena == null) {
                        arena = getPlayerArena((Player) sender);

                        if (arena == null) {
                            sender.sendMessage(getPrefix() + TextFormat.RED + "Arena '" + args[1] + "' doesn't exist");
                            return true;
                        }
                    }

                    arena.start(true);
                    sender.sendMessage(getPrefix() + TextFormat.GREEN + "Arena started!");
                    break;
                case "stop":
                    if (((sender instanceof Player) && args.length < 1) || (!(sender instanceof Player) && args.length != 2)) {
                        sender.sendMessage(ColorMatch.getPrefix() + TextFormat.GRAY + "Use " + TextFormat.YELLOW + " /cm start [arena]");
                        return true;
                    }

                    if (arena == null) {
                        arena = getPlayerArena((Player) sender);

                        if (arena == null) {
                            sender.sendMessage(getPrefix() + TextFormat.RED + "Arena '" + args[1] + "' doesn't exist");
                            return true;
                        }
                    }

                    arena.stop();
                    sender.sendMessage(getPrefix() + TextFormat.GREEN + "Arena stopped");
                    break;
                case "join":
                    if (!(sender instanceof Player)) {
                        sender.sendMessage(getPrefix() + TextFormat.RED + "Run this command in-game");
                        return true;
                    }

                    if (args.length != 2) {
                        sender.sendMessage(ColorMatch.getPrefix() + TextFormat.GRAY + "Use " + TextFormat.YELLOW + " /cm join <arena>");
                        return true;
                    }

                    if (arena == null) {
                        arena = getPlayerArena((Player) sender);

                        if (arena == null) {
                            sender.sendMessage(getPrefix() + TextFormat.RED + "Arena '" + args[1] + "' doesn't exist");
                            return true;
                        }
                    }

                    arena.addToArena((Player) sender);
                    break;
                case "leave":
                    if (!(sender instanceof Player)) {
                        sender.sendMessage(getPrefix() + TextFormat.RED + "Run this command in-game");
                        return true;
                    }

                    if (arena == null) {
                        arena = getPlayerArena((Player) sender);

                        if (arena == null) {
                            sender.sendMessage(getPrefix() + TextFormat.RED + "Arena '" + args[1] + "' doesn't exist");
                            return true;
                        }
                    }

                    if (arena.isSpectator((Player) sender)) {
                        arena.removeSpectator((Player) sender);
                    } else {
                        arena.removeFromArena((Player) sender);
                    }
                    break;
                case "set":
                    if (!(sender instanceof Player)) {
                        sender.sendMessage(getPrefix() + TextFormat.RED + "Run this command in-game");
                        return true;
                    }

                    if (args.length != 2) {
                        sender.sendMessage(ColorMatch.getPrefix() + TextFormat.GRAY + "Use " + TextFormat.YELLOW + " /cm set <arena>");
                        return true;
                    }

                    if (arena == null) {
                        sender.sendMessage(getPrefix() + TextFormat.RED + "Arena '" + args[1] + "' doesn't exist");
                        return true;
                    }

                    if (arena.getPhase() == Arena.PHASE_GAME) {
                        sender.sendMessage(getPrefix() + TextFormat.RED + "Arena is running");
                        return true;
                    }

                    arena.disable();
                    arena.setup = true;
                    setters.put(sender.getName(), arena);
                    giveSetupTools((Player) sender);
                    sender.sendMessage(getPrefix() + TextFormat.YELLOW + "You are now editing arena " + TextFormat.BLUE + arena.getName());
                    break;
                case "create":
                    if (args.length != 2) {
                        sender.sendMessage(ColorMatch.getPrefix() + TextFormat.GRAY + "Use " + TextFormat.YELLOW + " /cm create <arena>");
                        return true;
                    }

                    if (arena != null) {
                        sender.sendMessage(getPrefix() + TextFormat.RED + "Arena '" + args[1] + "' already exists");
                        break;
                    }

                    arena = new Arena(this, args[1].toLowerCase(), new File(getDataFolder() + "/arenas/" + args[1].toLowerCase() + ".yml"));

                    arenas.put(args[1].toLowerCase(), arena);

                    sender.sendMessage(getPrefix() + TextFormat.GREEN + "Arena " + TextFormat.YELLOW + "'" + args[1].toLowerCase() + "'" + TextFormat.GREEN + " was successfully created");
                    break;
                case "remove":
                    break;
                case "help":
                    sender.sendMessage(getHelp(sender, 0));
                    break;
                case "setlobby":
                case "setmainlobby":
                    if(args.length < 4){
                        if(!(sender instanceof Player)) {
                            sender.sendMessage(ColorMatch.getPrefix() + TextFormat.GRAY + "Use " + TextFormat.YELLOW + " /cm setlobby <x> <y> <z> [world]");
                            break;
                        } else {
                            this.conf.setMainLobby((Player) sender);
                        }
                    } else {
                        int x;
                        int y;
                        int z;

                        try {
                            x = Integer.valueOf(args[1]);
                            y = Integer.valueOf(args[2]);
                            z = Integer.valueOf(args[3]);
                        } catch (NumberFormatException e){
                            sender.sendMessage(getPrefix() +TextFormat.RED+ "Invalid arguments." +"\n"+getPrefix() + TextFormat.GRAY + "Use " + TextFormat.YELLOW + " /cm setlobby <x> <y> <z> [world]");
                            break;
                        }

                        Level level = null;

                        if(args.length > 4){
                            String world = args[4];

                            level = getServer().getLevelByName(world);

                            if(level ==  null){
                                if(!getServer().loadLevel(world)) {
                                    sender.sendMessage(getPrefix() + TextFormat.RED + "World '" + world + "' doesn't exist");
                                    break;
                                }

                                level = getServer().getLevelByName(world);
                            }
                        }

                        conf.setMainLobby(new Position(x, y, z, level != null ? level : getServer().getDefaultLevel()));
                    }
                    break;
                case "build":
                    if (args.length != 2 && args.length != 3) {
                        sender.sendMessage(ColorMatch.getPrefix() + TextFormat.GRAY + "Use " + TextFormat.YELLOW + " /cm build <arena> [block]");
                        return true;
                    }

                    Block b = new BlockIron();

                    if(args.length == 3){
                        Block b2 = Utils.fromString(args[2]);

                        if(b2.getId() == 0){
                            sender.sendMessage(getPrefix()+TextFormat.RED+"Invalid block");
                            return true;
                        } else {
                            b = b2;
                        }
                    }

                    if (arena == null) {
                        sender.sendMessage(getPrefix() + TextFormat.RED + "Arena '" + args[1] + "' doesn't exist");
                        return true;
                    }

                    sender.sendMessage(getPrefix() + TextFormat.GREEN + "Building arena " + TextFormat.YELLOW + arena.getName() + TextFormat.GREEN + "...");

                    sender.sendMessage(ArenaBuilder.build(arena, arena.getLevel(), b));
                    break;
                default:
                    sender.sendMessage(getHelp(sender, 1));
                    break;
            }
        }

        return true;
    }

    private String getHelp(CommandSender sender, int page) {
        String help = TextFormat.GRAY + "Showing ColorMatch help page 1/1:";

        if (sender.hasPermission("colormatch.command.start")) help += "\n" + TextFormat.YELLOW + "/cm start [arena]";
        if (sender.hasPermission("colormatch.command.stop")) help += "\n" + TextFormat.YELLOW + "/cm stop [arena]";
        if (sender.hasPermission("colormatch.command.join")) help += "\n" + TextFormat.YELLOW + "/cm join <arena>";
        if (sender.hasPermission("colormatch.command.leave")) help += "\n" + TextFormat.YELLOW + "/cm leave";
        if (sender.hasPermission("colormatch.command.enable")) help += "\n" + TextFormat.YELLOW + "/cm enable <arena>";
        if (sender.hasPermission("colormatch.command.disable")) help += "\n" + TextFormat.YELLOW + "/cm disable <arena>";
        if (sender.hasPermission("colormatch.command.disable")) help += "\n" + TextFormat.YELLOW + "/cm set <arena>";
        if (sender.hasPermission("colormatch.command.disable")) help += "\n" + TextFormat.YELLOW + "/cm create <arena>";
        if (sender.hasPermission("colormatch.command.disable")) help += "\n" + TextFormat.YELLOW + "/cm delete <arena>";
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

        inv.setItem(0, new ItemSwordGold().setCustomName(TextFormat.RESET + TextFormat.GREEN + "Set start position"));
        inv.setItem(1, new ItemPickaxeGold().setCustomName(TextFormat.RESET + TextFormat.GREEN + "Set floor position"));
        inv.setItem(2, new ItemAxeGold().setCustomName(TextFormat.RESET + TextFormat.GREEN + "Set spectator spawn"));
        inv.setItem(3, new ItemHoeGold().setCustomName(TextFormat.RESET + TextFormat.GREEN + "Set join sign"));

        for (int i = 0; i < 4; i++) {
            inv.setHotbarSlotIndex(i, i);
        }

        inv.sendContents(p);
    }

    private void initLanguage(){
        File languages = new File(getDataFolder()+"/lang");
        String lang = conf.getLanguage();

        if(!languages.exists() || !languages.isDirectory()) {
            getLogger().error("Could not load default language");
            return;
        } else {
            File[] files = languages.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.endsWith(".yml");
                }
            });

            File langFile = null;

            for(File l : files){
                if(l.getName().toLowerCase().equals(lang+".yml")){
                    langFile = l;
                    break;
                }
            }

            if(langFile == null){
                getLogger().warning("Could not find language file '"+lang+".yml'. Selecting English as default language");
                langFile = new File(getDataFolder()+"/lang/English.yml");
            }

            BaseLang baseLang = new BaseLang();
            baseLang.init(langFile.getPath());

            language = baseLang;
        }
    }

    private void initEconomy(){
        Plugin plugin = getServer().getPluginManager().getPlugin("EconomyAPI");

        if(plugin != null){
            economy = new EconomyAPIProvider(plugin);
        } else if((plugin = getServer().getPluginManager().getPlugin("Economy-LEET")) != null){
            economy = new LeetEconomyProvider(plugin);
        }
    }

    private void initStats() {
        String stats = conf.getStats();

        switch(stats.toLowerCase()){
            case "yml":
            case "yaml":
                this.stats = new YamlStatsProvider();
                break;
            case "sql":
            case "mysql":
                this.stats = new MySQLStatsProvider();
                break;
            default:
                this.stats = new NoneProvider();
        }

        if(!this.stats.init(this)){
            getLogger().warning("Could not load selected stats provider");
        }
    }
}
