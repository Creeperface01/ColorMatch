package com.creeperface.nukkitx.colormatch.lang;

import cn.nukkit.utils.Config;
import com.creeperface.nukkitx.colormatch.ColorMatch;

import java.util.Map;

public class BaseLang extends Config {

    private Map<String, Map<String, String>> messages = null;

    public boolean init(String path) {
        return load(path, Config.YAML);
    }

    public String translateString(String str) {
        return this.translateString(str, true);
    }

    public String translateString(String str, boolean addPrefix) {
        return this.translateString(str, addPrefix, new String[0]);
    }

    public String translateString(String str, String... params) {
        return translateString(str, true, params);
    }

    public String translateString(String str, boolean addPrefix, String... params) {
        String baseText = this.getString(str).replaceAll("&", "§");

        for (int i = 0; i < params.length; ++i) {
            baseText = baseText.replace("%" + i, params[i]);
        }

        return (addPrefix ? ColorMatch.getPrefix() : "") + baseText;
    }
}
