package cc.synkdev.nexusCore.bukkit;

import cc.synkdev.nexusCore.bukkit.objects.AnalyticsReport;
import cc.synkdev.nexusCore.bukkit.objects.PluginData;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

@SuppressWarnings("unused")
public class Analytics {
    private static final NexusCore core = NexusCore.getInstance();
    public static AnalyticsReport getCurrentReport() {
        AnalyticsReport rep = core.report;
        if (rep == null) core.report = new AnalyticsReport();
        return core.report;
    }

    public static void registerSpl(JavaPlugin pl) {
        if (!pl.getDescription().getAuthors().contains("Synk")) {
            return;
        }
        core.pls.add(pl);
        AnalyticsReport report = getCurrentReport();
        report.getSynkPlugins().putIfAbsent(pl.getDescription().getName(), new PluginData(pl));
    }

    public static void addCommandUse(JavaPlugin pl, String command) {
        AnalyticsReport rep = getCurrentReport();
        PluginData data = rep.getSynkPlugins().get(pl.getDescription().getName());
        data.getCommandUses().put(command, data.getCommandUses().getOrDefault(command, 0)+1);
        rep.getSynkPlugins().replace(pl.getDescription().getName(), data);
        core.report = rep;
    }

    public static void addCustomField(JavaPlugin pl, String field, Object value) {
        AnalyticsReport rep = getCurrentReport();
        PluginData data = rep.getSynkPlugins().get(pl.getDescription().getName());
        data.getFields().put(field, value);
        rep.getSynkPlugins().replace(pl.getDescription().getName(), data);
        core.report = rep;
    }


    public static void sendReport() {
        AnalyticsReport rep = getCurrentReport();
        rep.setTimestamp(System.currentTimeMillis());
        rep.setOnlinePlayers(Bukkit.getOnlinePlayers().size());

        try {
            URL url = new URL("https://analytics.synkdev.cc/api/ingest");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            String jsonPayload = rep.export().toString();

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            connection.getResponseCode();
            core.report = null;
        } catch (Exception ignored) {

        }
    }
}
