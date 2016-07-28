package ColorMatch.Lang;

import ColorMatch.ColorMatch;
import cn.nukkit.utils.Config;

public class BaseLang extends Config {

    public boolean init(String path){
        return load(path, Config.YAML);
    }

    public String translateString(String str) {
        return this.translateString(str, new String[0]);
    }

    public String translateString(String str, String... params) {
        String baseText = this.getString(str).replaceAll("&", "§");

        for(int i = 0; i < params.length; ++i) {
            baseText = baseText.replace("%" + i, params[i]);
        }

        return ColorMatch.getPrefix() + baseText;
    }
}
