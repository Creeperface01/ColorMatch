package main.java.ColorMatch.Arena;

import cn.nukkit.Player;
import cn.nukkit.blockentity.BlockEntitySign;
import cn.nukkit.potion.Effect;
import cn.nukkit.utils.TextFormat;

public abstract class ArenaManager extends Configuration {

    protected Arena plugin;

    public boolean inArena(Player p) {
        return plugin.players.containsKey(p.getName().toLowerCase());
    }

    public boolean isSpectator(Player p) {
        return plugin.spectators.containsKey(p.getName().toLowerCase());
    }

    public boolean checkLobby() {
        return plugin.players.size() >= plugin.plugin.conf.getMinPlayers();
    }

    public void onDeath(Player p) {

    }

    public void resetPlayer(Player p) {
        p.removeAllEffects();
        p.getInventory().clearAll();
    }

    public void messageArenaPlayers(String msg) {
        for (Player p : plugin.players.values()) {
            p.sendMessage(msg);
        }
    }

    /*public boolean checkAlive(){
        return plugin.players.size() > 0;
    }*/

    public String getTypeString(int type) {
        switch (type) {
            case TYPE_NORMAL:
                return "normal";
            case TYPE_FURIOUS:
                return "furious";
            case TYPE_STONED:
                return "stoned";
            case TYPE_BLIND:
                return "blind";
        }

        return "";
    }

    protected Effect getGameEffect() {
        switch (getType()) {
            case TYPE_BLIND:
                Effect.getEffect(Effect.BLINDNESS).setDuration(999999999).setVisible(false);
            case TYPE_FURIOUS:
                Effect.getEffect(Effect.SPEED).setAmplifier(4).setDuration(999999999).setVisible(false);
            case TYPE_STONED:
                return Effect.getEffect(Effect.CONFUSION).setDuration(999999999).setVisible(false);
        }

        return null;
    }

    public void updateJoinSign() {
        BlockEntitySign sign = (BlockEntitySign) level.getBlockEntity(getJoinSign());

        if (sign == null) {
            return;
        }

        sign.setText(TextFormat.GRAY + "[" + TextFormat.DARK_AQUA + plugin.name + TextFormat.GRAY + "]", TextFormat.GRAY + plugin.players.size() + "/" + plugin.plugin.conf.getMaxPlayers(), TextFormat.BLUE + getTypeString(getType()), plugin.phase == Arena.GAME ? TextFormat.RED + "running" : TextFormat.GREEN + "lobby");
    }

    public void checkAlive() {
        if (plugin.players.size() <= 1) {
            plugin.endGame();
        }
    }
}
