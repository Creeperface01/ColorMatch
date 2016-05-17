package main.java.ColorMatch;

import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.TextFormat;
import lombok.Getter;
import main.java.ColorMatch.Arena.Arena;

import java.io.File;
import java.io.FilenameFilter;
import java.util.HashMap;

public class ColorMatch extends PluginBase {

    public MainConfiguration conf;

    @Getter
    private HashMap<String, Arena> arenas = new HashMap<>();

    @Override
    public void onEnable() {
        new File(this.getDataFolder(), "arenas").mkdirs();
        this.saveResource("config.yml");

        this.conf = new MainConfiguration();
        if (!this.conf.init(this.getConfig())) {
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }

        this.registerArenas();
    }

    public static String getPrefix() {
        return "§9[§cCM§9] ";
    }

    public boolean registerArena(String name, Config config) {
        Arena arena = new Arena(this, name, config);
        arenas.put(name, arena);
        return arena.enable();
    }

    private void registerArenas() {
        this.getLogger().info(TextFormat.GREEN + "Loading arenas...");
        File arenas = new File(this.getDataFolder(), "arenas");

        if (!arenas.isDirectory()) {
            this.getLogger().info(TextFormat.GREEN + "No arenas found");
            return;
        }

        File[] files = arenas.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.lastIndexOf('.') > 0 && name.substring(name.lastIndexOf('.')).equals(".yml");
            }
        });

        if (files == null || files.length == 0) {
            this.getLogger().info(TextFormat.GREEN + "No arenas found");
            return;
        }

        for (File file : files) {
            Config config = new Config(file, Config.YAML);
            String fileName = file.getName().toLowerCase().trim();
            String name = fileName.substring(0, fileName.length() - 4);

            if (registerArena(name, config)) {
                this.getLogger().info(TextFormat.GRAY + name + TextFormat.GREEN + " load successful");
            } /*else {
                this.getLogger().info(TextFormat.GRAY + file.getName()+TextFormat.RED+" load failed");
            }*/
        }
    }

    public Arena getArena(String name) {
        return arenas.get(name.toLowerCase());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().toLowerCase().equals("colormatch")) {
            if (args.length <= 0) {
                sender.sendMessage("");
                return false;
            }

            switch (args[0].toLowerCase()) {
                case "start":
                    break;
                case "help":
                    break;
            }
        }

        return true;
    }
}
