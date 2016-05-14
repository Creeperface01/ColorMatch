package main.java.ColorMatch.Arena;

import cn.nukkit.Player;
import cn.nukkit.blockentity.BlockEntitySign;
import cn.nukkit.nbt.tag.CompoundTag;
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

    public void resetPlayer(Player p) {
        p.removeAllEffects();
        p.getInventory().clearAll();
        p.setExperience(0, 0);
        p.setHealth(20);
        p.getFoodData().setFoodSaturationLevel(20);
        p.getFoodData().setLevel(20);
    }

    public void messageArenaPlayers(String msg) {
        for (Player p : plugin.players.values()) {
            p.sendMessage(msg);
        }

        for (Player p : plugin.spectators.values()) {
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
        BlockEntitySign sign = (BlockEntitySign) getJoinSign().level.getBlockEntity(getJoinSign());

        if (sign == null) {
            CompoundTag nbt = (new CompoundTag()).putString("id", "Sign").putInt("x", (int) getJoinSign().x).putInt("y", (int) getJoinSign().y).putInt("z", (int) getJoinSign().z).putString("Text1", "").putString("Text2", "").putString("Text3", "").putString("Text4", "");

            sign = new BlockEntitySign(getJoinSign().level.getChunk((int) getJoinSign().x >> 4, (int) getJoinSign().z >> 4, true), nbt);
        }

        sign.setText(TextFormat.GRAY + "[" + TextFormat.DARK_AQUA + plugin.name + TextFormat.GRAY + "]", TextFormat.GRAY + plugin.players.size() + "/" + plugin.plugin.conf.getMaxPlayers(), TextFormat.BLUE + getTypeString(getType()), plugin.enabled ? plugin.phase == Arena.PHASE_GAME ? TextFormat.RED + "running" : TextFormat.GREEN + "lobby" : TextFormat.RED + "disabled");
    }

    public void checkAlive() {
        if (plugin.players.size() <= 1) {
            plugin.endGame();
        }
    }
}
