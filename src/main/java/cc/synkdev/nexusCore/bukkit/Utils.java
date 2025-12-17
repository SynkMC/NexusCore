package cc.synkdev.nexusCore.bukkit;

import cc.synkdev.nexusCore.components.NexusPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.io.*;
import java.net.URL;

public class Utils implements Listener {
    private final NexusCore core = NexusCore.getInstance();
    NexusPlugin spl;
    public Utils(NexusPlugin spl) {
        this.spl = spl;
    }
    public static void log(String s) {
        Bukkit.getConsoleSender().sendMessage(NexusCore.getPl().prefix()+" "+s);
    }

    public static FileConfiguration loadWebConfig(String url, File file) {
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        File temp = new File(file.getParentFile(), "temp-"+System.currentTimeMillis()+".yml");
        try {
            URL uri = new URL(url);
            if (!temp.exists()) temp.createNewFile();
            BufferedReader reader = new BufferedReader(new InputStreamReader(uri.openStream()));

            BufferedWriter writer = new BufferedWriter(new FileWriter(temp));

            String line;
            while ((line = reader.readLine()) != null) {
                String[] lines = line.split("<br>");
                for (String liness : lines) {
                    String[] split = liness.split(";");
                    if (split.length == 3) {
                        if (!config.contains(split[1])) {
                            writer.write("# " + split[0]);
                            writer.newLine();
                            writer.write(split[1] + ": " + split[2]);
                            writer.newLine();
                        } else {
                            writer.write("# " + split[0]);
                            writer.newLine();
                            writer.write(split[1] + ": " + config.get(split[1]));
                            writer.newLine();
                        }
                    } else {
                        if (!config.contains(split[0])) {
                            writer.write(split[0] + ": " + split[1]);
                            writer.newLine();
                        } else {
                            writer.write(split[0] + ": " + config.get(split[0]));
                            writer.newLine();
                        }
                    }
                }
            }

            reader.close();
            writer.close();

            copyFile(temp, file);

            temp.delete();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return config;
    }

    public static void copyFile(File source, File destination) throws IOException {
        try (InputStream in = new FileInputStream(source);
             OutputStream out = new FileOutputStream(destination)) {

            byte[] buffer = new byte[1024];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
        }
    }

    @EventHandler
    public void join (PlayerJoinEvent event) {
        if (event.getPlayer().isOp()) NexusCore.availableUpdates.forEach((s, s2) -> {
            Player p = event.getPlayer();
            p.sendMessage(core.prefix+ChatColor.GOLD+Lang.translate("updateAvailable", s) + " "+s+"!");
            p.sendMessage(core.prefix+ChatColor.GOLD+Lang.translate("downloadHere", s)+": "+s.dlLink());
        });
    }
}
