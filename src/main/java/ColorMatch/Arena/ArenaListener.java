package ColorMatch.Arena;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockSignPost;
import cn.nukkit.blockentity.BlockEntitySign;
import cn.nukkit.command.CommandSender;
import cn.nukkit.entity.Entity;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.block.BlockBreakEvent;
import cn.nukkit.event.block.BlockPlaceEvent;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.event.player.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

class ArenaListener implements Listener {

    private Arena plugin;

    public ArenaListener(Arena plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();

        if (plugin.inArena(p)) {
            plugin.removeFromArena(p);
        } else if (plugin.isSpectator(p)) {
            plugin.removeSpectator(p);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInteract(PlayerInteractEvent e) {
        int action = e.getAction();

        if (e.isCancelled()) {
            return;
        }

        if (action != PlayerInteractEvent.RIGHT_CLICK_BLOCK && action != PlayerInteractEvent.LEFT_CLICK_BLOCK) {
            return;
        }

        Player p = e.getPlayer();
        Block b = e.getBlock();

        if (plugin.inArena(p)) {
            e.setCancelled();
            return;
        }

        if (b instanceof BlockSignPost && b.equals(plugin.getJoinSign())) {
            e.setCancelled();

            if (!p.hasPermission("colormatch.sign.use")) {
                p.sendMessage(plugin.plugin.getLanguage().translateString("permission_message", false));
                return;
            }
            plugin.addToArena(p);
            e.setCancelled();
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        Player p = e.getPlayer();
        Block b = e.getBlock();

        if (plugin.inArena(p) || plugin.isSpectator(p)) {
            e.setCancelled();
            return;
        }

        if (b instanceof BlockSignPost) {
            BlockEntitySign sign = (BlockEntitySign) b.getLevel().getBlockEntity(b);

            if (sign == null) {
                return;
            }

            String line1 = sign.getText()[0];

            if (b.equals(plugin.getJoinSign())) {
                if (!p.hasPermission("colormatch.sign.break")) {
                    p.sendMessage(plugin.plugin.getLanguage().translateString("permission_message", false));
                    e.setCancelled();
                }
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        Player p = e.getPlayer();

        if (plugin.inArena(p) || plugin.isSpectator(p)) {
            e.setCancelled();
        }
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent e) {
        Player p = e.getPlayer();

        if (plugin.inArena(p) || plugin.isSpectator(p)) {
            e.setCancelled();
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onChat(PlayerChatEvent e) {
        Player p = e.getPlayer();
        String msg = e.getMessage();

        if (e.isCancelled()) {
            return;
        }

        Set<CommandSender> recipients = e.getRecipients();
        String prefix = "";

        if (plugin.inArena(p)) {
            prefix = plugin.plugin.conf.getGameChatFormat();
            /*String lastColor = "f";

            if(Utils.getLastColor(msg).toLowerCase().equals("f")){
                lastColor = Utils.getLastColor(p.getDisplayName().toLowerCase());
            }

            if(e.getMessage().lastIndexOf() !p.getDisplayName().toLowerCase().trim().substring(Math.max(0, p.getDisplayName().length() - 5)).contains(TextFormat.WHITE)) {
                e.setMessage(TextFormat.GRAY + e.getMessage());
            }*/
        } else if (plugin.isSpectator(p)) {
            prefix = plugin.plugin.conf.getSpectatorChatFormat();
        } else {
            for (CommandSender sender : new HashSet<>(recipients)) {
                if (!(sender instanceof Player)) {
                    continue;
                }

                Player s = (Player) sender;

                if (!plugin.inArena(s) && !plugin.isSpectator(s)) {
                    continue;
                }

                recipients.remove(sender);
            }
            return;
        }

        e.setCancelled();
        plugin.messageArenaPlayers(prefix.replaceAll("\\{PLAYER}", p.getDisplayName()).replaceAll("\\{MESSAGE}", e.getMessage()));
    }

    private static ArrayList<Integer> allowedCauses = new ArrayList<>(Arrays.asList(EntityDamageEvent.CAUSE_VOID, EntityDamageEvent.CAUSE_FALL, EntityDamageEvent.CAUSE_FIRE, EntityDamageEvent.CAUSE_FIRE_TICK, EntityDamageEvent.CAUSE_LAVA, EntityDamageEvent.CAUSE_CONTACT));

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = false)
    public void onDamage(EntityDamageEvent e) {
        if (e.isCancelled()) {
            return;
        }

        Entity entity = e.getEntity();
        Player p;
        int cause = e.getCause();

        if (entity instanceof Player) {
            p = (Player) entity;

            if (plugin.isSpectator(p)) {
                e.setCancelled();
                return;
            }

            if (plugin.inArena(p)) {
                if (plugin.phase == Arena.PHASE_LOBBY || !allowedCauses.contains(cause)) {
                    e.setCancelled();
                    return;
                } else if (e.getFinalDamage() >= p.getHealth()) {
                    e.setCancelled();
                    plugin.onDeath(p);
                    return;
                }
            }
        }

        if (e instanceof EntityDamageByEntityEvent) {
            if (((EntityDamageByEntityEvent) e).getDamager() instanceof Player) {
                Player damager = (Player) ((EntityDamageByEntityEvent) e).getDamager();

                if ((plugin.inArena(damager) || plugin.isSpectator(damager))) {
                    e.setCancelled();
                }
            }
        }
    }

    @EventHandler
    public void onFoodChange(PlayerFoodLevelChangeEvent e) {
        Player p = e.getPlayer();

        if (plugin.inArena(p) || plugin.isSpectator(p)) {
            e.setCancelled();
        }
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent e) {
        Player p = e.getPlayer();
        String cmd = e.getMessage();

        if (!p.isOp() && !cmd.toLowerCase().startsWith("/cm") && (plugin.inArena(p) || plugin.isSpectator(p))) {
            p.sendMessage(plugin.plugin.getLanguage().translateString("game.commands"));
            e.setCancelled();
        }
    }
}
