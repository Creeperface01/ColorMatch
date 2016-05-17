package main.java.ColorMatch.Utils;

import cn.nukkit.utils.TextFormat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WoolColor {
    //private static String[] colors = new String[]{TextFormat.BLACK + "black", TextFormat.RED + "red", TextFormat.DARK_GREEN + "green", TextFormat.BLACK + "brown", TextFormat.DARK_BLUE + "blue", TextFormat.DARK_PURPLE + "purple", TextFormat.BLUE + "cyan", TextFormat.GRAY + "silver", TextFormat.DARK_GRAY + "gray", TextFormat.LIGHT_PURPLE + "pink", TextFormat.GREEN + "lime", TextFormat.YELLOW + "yellow", TextFormat.BLUE + "light blue", TextFormat.LIGHT_PURPLE + "magenta", TextFormat.GOLD + "orange", "white"};
    private static String[] colors = new String[]{"white", TextFormat.GOLD + "orange", TextFormat.LIGHT_PURPLE + "magenta", TextFormat.BLUE + "light blue", TextFormat.YELLOW + "yellow", TextFormat.GREEN + "lime", TextFormat.LIGHT_PURPLE + "pink", TextFormat.DARK_GRAY + "gray", TextFormat.GRAY + "silver", TextFormat.BLUE + "cyan", TextFormat.DARK_PURPLE + "purple", TextFormat.DARK_BLUE + "blue", TextFormat.BLACK + "brown", TextFormat.DARK_GREEN + "green", TextFormat.RED + "red", TextFormat.BLACK + "black"};

    public static String getColorName(int meta) {
        return colors[meta];
    }
}
