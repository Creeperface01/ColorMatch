package ColorMatch.Arena;

import cn.nukkit.Player;
import cn.nukkit.item.Item;
import cn.nukkit.potion.Effect;

import java.util.HashMap;
import java.util.Map;

public class SavedPlayer {

    private int health = 0;
    private int xp = 0;
    private int xpLevel = 0;
    private int hunger = 0;
    private float hungerSaturation = 0;
    private int gamemode = 0;
    private Map<Integer, Effect> effects = null;

    private Map<Integer, Item> items = null;
    private int[] hotbar = null;

    public void save(Player p) {
        health = p.getHealth();
        xp = p.getExperience();
        xpLevel = p.getExperienceLevel();
        hunger = p.getFoodData().getLevel();
        hungerSaturation = p.getFoodData().getFoodSaturationLevel();
        gamemode = p.getGamemode();
        effects = new HashMap<>(p.getEffects());

        items = new HashMap<>(p.getInventory().getContents());

        hotbar = new int[p.getInventory().getHotbarSize()];

        for (int i = 0; i < hotbar.length; i++) {
            hotbar[i] = p.getInventory().getHotbarSlotIndex(i);
        }
    }

    public void load(Player p) {
        p.setHealth(health);
        p.setExperience(xp, xpLevel);
        p.getFoodData().setLevel(hunger, hungerSaturation);
        p.setGamemode(gamemode);

        effects.forEach((i, e) -> p.addEffect(e));

        p.getInventory().clearAll();
        p.getInventory().setContents(items);

        for (int i = 0; i < hotbar.length; i++) {
            p.getInventory().setHotbarSlotIndex(hotbar[i], i);
        }

        p.getInventory().sendContents(p);
        p.getInventory().sendArmorContents(p);
    }
}
