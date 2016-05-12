package main.java.ColorMatch;

import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerJoinEvent;

public class MainListener implements Listener {

    private ColorMatch plugin;

    public MainListener(ColorMatch plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {

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
}
