package ColorMatch.Arena;

import cn.nukkit.Player;
import cn.nukkit.block.BlockWallSign;
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

    public void checkLobby() {
        if (plugin.players.size() >= plugin.plugin.conf.getMinPlayers()) {
            plugin.starting = true;
        }
    }

    public void resetPlayer(Player p) {
        p.removeAllEffects();
        p.getInventory().clearAll();
        p.setExperience(0, 0);
        p.setHealth(20);
        p.getFoodData().setFoodSaturationLevel(20);
        p.getFoodData().setLevel(20);
        p.extinguish();
    }

    public void messageArenaPlayers(String msg) {
        plugin.players.values().forEach((Player p) -> p.sendMessage(msg));
        plugin.spectators.values().forEach((Player p) -> p.sendMessage(msg));
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
                return Effect.getEffect(Effect.BLINDNESS).setDuration(999999999).setVisible(false);
            case TYPE_FURIOUS:
                return Effect.getEffect(Effect.SPEED).setAmplifier(4).setDuration(999999999).setVisible(false);
            case TYPE_STONED:
                return Effect.getEffect(Effect.CONFUSION).setDuration(999999999).setVisible(false);
        }

        return null;
    }

    public void updateJoinSign() {
        BlockEntitySign sign = (BlockEntitySign) getJoinSign().level.getBlockEntity(getJoinSign());

        if (sign == null) {
            //getJoinSign().level.setBlock(getJoinSign(), new BlockWallSign(), true, false);

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
