package cc.synkdev.nexusCore.bukkit;

import cc.synkdev.nexusCore.components.PluginUpdate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class UpdateChecker {
    public static JSONObject readData() {
        try {
            URL url = new URL("https://synkdev.cc/storage/versions.json");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder content = new StringBuilder();
            String ln;
            while ((ln = in.readLine()) != null) {
                content.append(ln);
            }
            in.close();
            conn.disconnect();
            return new JSONObject(content.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static List<PluginUpdate> checkOutated() {
        NexusCore.setPl(NexusCore.getInstance());
        List<Plugin> synkPlugs = new ArrayList<>();
        for (Plugin pl : Bukkit.getPluginManager().getPlugins()) {
            if (pl.getDescription().getAuthors().contains("Synk") || pl.getDescription().getAuthors().contains("Riddles")) {
                synkPlugs.add(pl);
            }
        }

        List<PluginUpdate> list = new ArrayList<>();
        JSONObject obj = readData();
        for (Plugin pl : synkPlugs) {
            if (obj.has(pl.getName())) {
                JSONObject plObj = obj.getJSONObject(pl.getName());
                String ver = plObj.getString("version");
                if (!pl.getDescription().getVersion().equals(ver)) {
                    String link = plObj.getString("link");
                    File file = null;
                    for (File lF : pl.getDataFolder().getParentFile().listFiles()) {
                        if (file == null) {
                            if (lF.getName().contains(pl.getName()) && lF.getName().contains(".jar")) {
                                file = lF;
                            }
                        }
                    }
                    String name = pl.getName().equalsIgnoreCase("SynkLibs") ? "NexusCore" : pl.getName();
                    if (file != null) list.add(new PluginUpdate(ver, name, link, file));
                }
            }
        }
        return list;
    }
    public static void update(List<PluginUpdate> list) {
        for (PluginUpdate pl : list) {
            try {
                pl.getCurrent().delete();
                URL url = new URL(pl.getDl());
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    try (InputStream in = conn.getInputStream();
                         OutputStream out = Files.newOutputStream(new File(pl.getCurrent().getParentFile(), pl.getPlugin() + ".jar").toPath())) {

                        byte[] buffer = new byte[4096];
                        int read;
                        while ((read = in.read(buffer)) != -1) {
                            out.write(buffer, 0, read);
                        }
                        out.flush();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                }
                conn.disconnect();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        Utils.log(NexusCore.getInstance().prefix+ ChatColor.GOLD+"New plugin versions have been downloaded! Restart your server for the changes to apply.");
    }

}
