package ColorMatch.EventHandler;

import ColorMatch.Arena.Arena;
import ColorMatch.ColorMatch;
import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockSignPost;
import cn.nukkit.blockentity.BlockEntitySign;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.block.BlockBreakEvent;
import cn.nukkit.event.block.SignChangeEvent;
import cn.nukkit.event.player.PlayerChatEvent;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.event.player.PlayerJoinEvent;
import cn.nukkit.event.player.PlayerQuitEvent;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemTool;
import cn.nukkit.utils.TextFormat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainListener implements Listener {

    private ColorMatch plugin;

    public MainListener(ColorMatch plugin) {
        this.plugin = plugin;
    }

    private List<String> acceptQueue = new ArrayList<>();

    private Map<String, Integer> setters = new HashMap<>();

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();

        plugin.getStats().createNewUser(p.getName().toLowerCase());

        if (plugin.getSetters().containsKey(p.getName())) {
            p.sendMessage(plugin.getLanguage().translateString("setupmode.continue_join", plugin.getSetters().get(p.getName()).getName()));
        }
    }

    /*public void onChat(PlayerChatEvent e){
        Set<CommandSender> recipients = new HashSet<>();

        for(CommandSender sender : recipients){
            if(plugin.players.containsKey(sender.getName().toLowerCase()) || plugin.spectators.containsKey(sender.getName().toLowerCase())){
                recipients.remove(sender);
            }
        }

        for(Arena arena : plugin.getArenas().values()){
            for(Player p : arena.getPlayers().values()){
                recipients.remove(p);
            }
        }

        e.setRecipients(recipients);
        return;
    }*/

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        Block b = e.getBlock();

        if (e.isCancelled()) {
            return;
        }

        if (b instanceof BlockSignPost) {
            BlockEntitySign sign = (BlockEntitySign) b.getLevel().getBlockEntity(b);

            if (sign == null) {
                return;
            }

            String line1 = sign.getText()[0];

            if (TextFormat.clean(line1.toLowerCase()).trim().equals("[cm]")) {
                e.setCancelled();

                if (!p.hasPermission("colormatch.sign.use")) {
                    p.sendMessage(plugin.getLanguage().translateString("general.permission_message"));
                    return;
                }

                String name = TextFormat.clean(sign.getText()[1]).trim().toLowerCase();
                Arena arena = plugin.getPlayerArena(p);

                if (name.equals("leave")) {
                    if (arena != null) {
                        if (arena.isSpectator(p)) {
                            arena.removeSpectator(p);
                        } else {
                            arena.removeFromArena(p);
                        }
                    }
                } else {
                    arena = plugin.getArena(name);

                    if (arena != null) {
                        arena.addToArena(p);
                    } else {
                        p.sendMessage(plugin.getLanguage().translateString("general.arena_doesnt_exist"));
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(PlayerChatEvent e) {
        if (e.isCancelled()) {
            return;
        }

        Player p = e.getPlayer();
        Arena arena = plugin.getSetters().get(p.getName());

        if (arena != null) {
            e.setCancelled();
            String[] args = e.getMessage().split(" ");

            if (acceptQueue.contains(p.getName())) {
                switch (args[0].toLowerCase().trim()) {
                    case "yes":
                        acceptQueue.remove(p.getName());

                        arena.save(true);
                        p.sendMessage(plugin.getLanguage().translateString("setupmode.save"));

                        plugin.getSetters().remove(p.getName());
                        break;
                    case "no":
                        acceptQueue.remove(p.getName());
                        p.sendMessage(plugin.getLanguage().translateString("setupmode.continue"));
                        break;
                }

                acceptQueue.remove(p.getName());
                return;
            }

            switch (args[0].toLowerCase().trim()) {
                /*case "joinsign":
                    setters.put(p.getName(), JOIN_SIGN);
                    p.sendMessage(ColorMatch.getPrefix()+TextFormat.GREEN+"Now break a block to set join sign");
                    break;
                case "startpos":
                case "spawn":
                    setters.put(p.getName(), START_POS);
                    p.sendMessage(ColorMatch.getPrefix()+TextFormat.GREEN+"Now break a block to set start position");
                    break;
                case "specspawn":
                case "spectatorspawn":
                    setters.put(p.getName(), SPEC_POS);
                    p.sendMessage(ColorMatch.getPrefix()+TextFormat.GREEN+"Now break a block to set spectator spawn");
                    break;
                case "floorposition":
                case "floorpos":
                    setters.put(p.getName(), FLOOR_POS);
                    p.sendMessage(ColorMatch.getPrefix()+TextFormat.GREEN+"Now break a block to set floor center position");
                    break;*/
                case "radius":
                case "floorradius":
                    if (args.length != 2) {
                        p.sendMessage(plugin.getLanguage().translateString("setupmode.help.radius"));
                        break;
                    }

                    int radius;

                    try {
                        radius = Integer.valueOf(args[1]);
                    } catch (NumberFormatException nfe) {
                        p.sendMessage(plugin.getLanguage().translateString("setupmode.failure.number_error"));
                        break;
                    }

                    arena.setRadius(radius);
                    p.sendMessage(plugin.getLanguage().translateString("setupmode.radius", String.valueOf(radius)));
                    break;
                case "floortype":
                    if (args.length != 2) {
                        p.sendMessage(plugin.getLanguage().translateString("setupmode.help.floor_type"));
                        break;
                    }

                    String type = args[1].toLowerCase().trim();

                    if (!(type.equals("wool") || type.equals("carpet") || type.equals("clay"))) {
                        p.sendMessage(plugin.getLanguage().translateString("setupmode.failure.floor_type_error"));
                        break;
                    }

                    arena.setFloorType(args[1]);
                    p.sendMessage(plugin.getLanguage().translateString("setupmode.floor_type", args[1]));
                    break;
                case "colortime":
                case "colorinterval":
                case "colorchangeinterval":
                    if (args.length != 2) {
                        p.sendMessage(plugin.getLanguage().translateString("setupmode.colortime"));
                        break;
                    }

                    int interval;

                    try {
                        interval = Integer.valueOf(args[1]);
                    } catch (NumberFormatException nfe) {
                        p.sendMessage(plugin.getLanguage().translateString("setupmode.failure.number_error"));
                        break;
                    }

                    arena.setColorChangeInterval(interval);
                    p.sendMessage(plugin.getLanguage().translateString("setupmode.colortime"));
                    break;
                case "type":
                case "arenatype":
                    if (args.length != 2) {
                        p.sendMessage(plugin.getLanguage().translateString("setupmode.help.game_type"));
                        break;
                    }

                    String type2 = args[1].toLowerCase().trim();

                    int arenaType = -1;

                    switch (type2) {
                        case "0":
                        case "normal":
                        case "default":
                            arenaType = 0;
                            break;
                        case "1":
                        case "furious":
                            arenaType = 1;
                            break;
                        case "2":
                        case "stoned":
                            arenaType = 2;
                            break;
                        case "3":
                        case "blind":
                            arenaType = 3;
                            break;
                    }

                    if (arenaType == -1) {
                        p.sendMessage(plugin.getLanguage().translateString("setupmode.failure.arena_type_error"));
                        break;
                    }

                    arena.setType(arenaType);
                    p.sendMessage(plugin.getLanguage().translateString("setupmode.game_type"));
                    break;
                /*case "level":
                case "world":
                case "arenaworld":
                    if (args.length != 2) {
                        p.sendMessage(ColorMatch.getPrefix() + TextFormat.YELLOW + "use 'world <world>'");
                        break;
                    }

                    File folder = new File(plugin.getServer().getDataPath() + "worlds/" + args[1]);

                    if (!folder.exists() || !folder.isDirectory()) {
                        p.sendMessage(ColorMatch.getPrefix() + TextFormat.YELLOW + "World '" + args[1] + "' doesn't exist and will be generated after editing");
                    }

                    arena.setWorld(args[1]);

                    Level lvl = plugin.getServer().getLevelByName(args[1]);

                    if(lvl == null){
                        if(!plugin.getServer().isLevelGenerated(args[1])){
                            plugin.getServer().generateLevel(args[1], new Random().nextLong(), Generator.getGenerator(Generator.TYPE_FLAT));
                        }

                        plugin.getServer().loadLevel(args[1]);
                        lvl = plugin.getServer().getLevelByName(args[1]);
                    }

                    if(lvl != null){
                        arena.setLevel(lvl);
                    }

                    p.sendMessage(ColorMatch.getPrefix() + TextFormat.GREEN + "Set arena world to " + TextFormat.YELLOW + args[1]);
                    break;*/
                case "done":
                    List<String> fields = arena.checkConfiguration();

                    if (!fields.isEmpty()) {
                        String message = plugin.getLanguage().translateString("setupmode.failure.missing_arguments", String.join(", ", fields));
                        //message += "\n" + TextFormat.YELLOW + "Do you want to save the arena? <yes/no>";

                        p.sendMessage(message);

                        acceptQueue.add(p.getName());
                    } else {
                        arena.save(true);
                        arena.setup = false;
                        p.sendMessage(plugin.getLanguage().translateString("setupmode.save", arena.getName()));
                        p.getInventory().clearAll();
                        if (p.isSurvival()) {
                            p.setAllowFlight(false);
                        }
                        plugin.getSetters().remove(p.getName());
                    }
                    break;
                case "help":
                    String msg = "";

                    msg += plugin.getLanguage().translateString("setupmode.help.help", "1", "1");
                    msg += "\n" + TextFormat.YELLOW + "   floorradius <radius>";
                    msg += "\n" + TextFormat.YELLOW + "   floortype <wool/carpet/clay>";
                    msg += "\n" + TextFormat.YELLOW + "   colorinterval <seconds>";
                    msg += "\n" + TextFormat.YELLOW + "   arenatype <normal/blind/stoned/furious>";
                    msg += "\n" + TextFormat.YELLOW + "   arenaworld <world>";

                    p.sendMessage(msg);
                    break;
                default:
                    p.sendMessage(plugin.getLanguage().translateString("setupmode.failure.unknown_command"));
                    break;
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        if (acceptQueue.contains(e.getPlayer().getName())) {
            acceptQueue.remove(e.getPlayer().getName());
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        Player p = e.getPlayer();
        Block b = e.getBlock();
        Item item = e.getItem();

        if (b instanceof BlockSignPost) {
            BlockEntitySign sign = (BlockEntitySign) b.getLevel().getBlockEntity(b);

            if (sign == null) {
                return;
            }

            String line1 = sign.getText()[0];

            if (TextFormat.clean(line1.toLowerCase()).trim().equals("[cm]")) {
                if (!p.hasPermission("colormatch.sign.break")) {
                    p.sendMessage(plugin.getLanguage().translateString("general.permission_message"));
                    e.setCancelled();
                }
            }
        }

        Arena arena = plugin.getSetters().get(p.getName());

        if (arena != null) {
            if (item.isTool() && item.getTier() == ItemTool.TIER_GOLD) {
                e.setCancelled();

                switch (TextFormat.clean(item.getCustomName()).toLowerCase().trim()) {
                    case "start position":
                        item.setCustomName(""+TextFormat.RESET + TextFormat.RED + "Start position");
                        p.getInventory().setItemInHand(item);
                        p.getInventory().sendHeldItem(p);

                        arena.setStartPos(b);
                        p.sendMessage(plugin.getLanguage().translateString("setupmode.startpos", String.valueOf(b.x), String.valueOf(b.y), String.valueOf(b.z)));
                        break;
                    case "floor position":
                        item.setCustomName(""+TextFormat.RESET + TextFormat.RED + "Floor position");
                        p.getInventory().setItemInHand(item);
                        p.getInventory().sendHeldItem(p);

                        arena.setFloorPos(b);
                        arena.recalculateBoundingBox();
                        arena.setWorld(b.getLevel().getFolderName());
                        arena.setLevel(b.getLevel());
                        p.sendMessage(plugin.getLanguage().translateString("setupmode.floorpos", String.valueOf(b.x), String.valueOf(b.y), String.valueOf(b.z)));
                        break;
                    case "spectator spawn":
                        item.setCustomName(""+TextFormat.RESET + TextFormat.RED + "Spectator spawn");
                        p.getInventory().setItemInHand(item);
                        p.getInventory().sendHeldItem(p);

                        arena.setSpectatorPos(b);
                        p.sendMessage(plugin.getLanguage().translateString("setupmode.spectatorspawn", String.valueOf(b.x), String.valueOf(b.y), String.valueOf(b.z)));
                        break;
                    case "join sign":
                        item.setCustomName(""+TextFormat.RESET + TextFormat.RED + "Join sign");
                        p.getInventory().setItemInHand(item);
                        p.getInventory().sendHeldItem(p);

                        arena.setJoinSign(b);
                        p.sendMessage(plugin.getLanguage().translateString("setupmode.joinsign", String.valueOf(b.x), String.valueOf(b.y), String.valueOf(b.z)));
                        break;
                    default:
                        e.setCancelled(false);
                        break;
                }
            }
        }
    }

    @EventHandler
    public void onSignChange(SignChangeEvent e) {
        Player p = e.getPlayer();
        String line1 = e.getLine(0);

        if (TextFormat.clean(line1.toLowerCase()).trim().equals("[cm]")) {
            if (!p.hasPermission("colormatch.sign.create")) {
                e.setCancelled();
                p.sendMessage(plugin.getLanguage().translateString("general.permission_message"));
                return;
            }

            String line2 = TextFormat.clean(e.getLine(1)).toLowerCase();

            if (line2.equals("leave")) {
                e.setLine(0, "");
                e.setLine(1, ColorMatch.getPrefix());
                e.setLine(2, TextFormat.GRAY + "leave");
                e.setLine(3, "");
            } else {

                Arena arena = plugin.getArena(line2);

                if (arena == null) {
                    p.sendMessage(plugin.getLanguage().translateString("general.arena_doesnt_exist"));
                    e.setCancelled();
                    return;
                }

                e.setLine(0, ColorMatch.getPrefix());
                e.setLine(1, TextFormat.DARK_AQUA + arena.getName().substring(0, 1).toUpperCase() + arena.getName().substring(1).toLowerCase());
                e.setLine(2, TextFormat.BLUE + arena.getTypeString(arena.getType()));
                e.setLine(3, "");
            }

            p.sendMessage(plugin.getLanguage().translateString("general.create_sign"));
        }
    }
}
