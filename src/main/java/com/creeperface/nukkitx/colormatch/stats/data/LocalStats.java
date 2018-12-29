package com.creeperface.nukkitx.colormatch.stats.data;

import com.creeperface.nukkitx.colormatch.ColorMatch;

import java.util.EnumMap;
import java.util.Map;

/**
 * Created by CreeperFace on 1.7.2017.
 */
public class LocalStats {

    private final EnumMap<Stat, Integer> stats = new EnumMap<>(Stat.class);

    public void init(Map<String, Integer> data) {
        int count = 0;

        for (Stat stat : Stat.values()) {
            try {
                stats.put(stat, data.get(stat.name().toLowerCase()));
            } catch (Exception e) {
                stats.put(stat, 0);
                count++;
            }
        }

        if (count > 0) {
            ColorMatch.getInstance().getLogger().warning(count + " errors happened while loading player stats");
        }
    }

    public enum Stat {
        WINS,
        DEATHS,
        ROUNDS
    }
}
