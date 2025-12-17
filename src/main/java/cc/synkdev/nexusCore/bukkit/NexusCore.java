package cc.synkdev.nexusCore.bukkit;

import cc.synkdev.nexusCore.bukkit.commands.ReportCmd;
import cc.synkdev.nexusCore.bukkit.commands.NcCmd;
import cc.synkdev.nexusCore.bukkit.objects.AnalyticsReport;
import cc.synkdev.nexusCore.components.PluginUpdate;
import cc.synkdev.nexusCore.components.NexusPlugin;
import co.aikar.commands.BukkitCommandManager;
import lombok.Getter;
import lombok.Setter;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

@SuppressWarnings("ResultOfMethodCallIgnored")
public final class NexusCore extends JavaPlugin implements NexusPlugin {
    @Getter private static NexusCore instance;
    @Setter String prefix = ChatColor.translateAlternateColorCodes('&', "&8[&6NexusCore&8] » &r");
    @Setter @Getter static NexusPlugin pl = null;
    public static Map<NexusPlugin, String> availableUpdates = new HashMap<>();
    private final File configFile = new File(getDataFolder(), "config.yml");
    public FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
    public static String lang = "en";
    @Getter @Setter private static Boolean loopReport = false;
    public static Map<String, String> langMap = new HashMap<>();
    public List<PluginUpdate> outdated = new ArrayList<>();
    public Boolean doAnalytics = true;
    public Boolean doAutoUpdate = true;
    public UUID serverUUID;
    public AnalyticsReport report;
    public List<JavaPlugin> pls = new ArrayList<>();

    @Override
    public void onLoad() {
        instance = this;
        setPl(this);
    }

    @Override
    public void onEnable() {
        loadConfig();
        loadAnalytics();

        langMap.clear();
        langMap.putAll(Lang.init(this, new File(getDataFolder(), "lang.json")));

        BukkitCommandManager pcm = new BukkitCommandManager(this);

        setPl(this);
        Metrics metrics = new Metrics(this, 23015);
        metrics.addCustomChart(new SimplePie("lang", () -> config.getString("lang", "en")));
        Bukkit.getPluginManager().registerEvents(new Utils(this), this);

        pcm.registerCommand(new ReportCmd(this));
        pcm.registerCommand(new NcCmd(this));


        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
            outdated.clear();
            outdated.addAll(UpdateChecker.checkOutated());
            if (!outdated.isEmpty() && doAutoUpdate) UpdateChecker.update(outdated);
        }, 0L, 12000L);
        
        if (doAnalytics) {
            Bukkit.getScheduler().runTaskTimerAsynchronously(this, Analytics::sendReport, 0L, 10*60*20L);
        }
    }

    public void loadConfig() {
        try {
            File slFolder = new File(getDataFolder().getParentFile(), "SynkLibs");
            if (slFolder.exists()) {
                slFolder.renameTo(getDataFolder());
            }

            if (!configFile.getParentFile().exists()) configFile.getParentFile().mkdirs();
            if (!configFile.exists()) {
                configFile.createNewFile();
            }

            config = YamlConfiguration.loadConfiguration(configFile);
            config = Utils.loadWebConfig("https://synkdev.cc/storage/config-libs.php", configFile);
            lang = config.getString("lang");
            doAutoUpdate = config.getBoolean("autoupdate");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void loadAnalytics() {
        File analyticsFile = new File(getDataFolder(), "analytics.yml");

        try {
            if (!analyticsFile.exists()) {
                Files.copy(getResource("analytics.yml"), analyticsFile.toPath());
            }
            YamlConfiguration analyticsConfig = YamlConfiguration.loadConfiguration(analyticsFile);
            doAnalytics = analyticsConfig.getBoolean("agree");
            String uuid = analyticsConfig.getString("uuid");
            UUID uid;
            boolean changed = false;
            try {
                uid = UUID.fromString(uuid);
            } catch (IllegalArgumentException e) {
                uid = UUID.randomUUID();
                changed = true;
            }
            serverUUID = uid;
            if (changed) {
                analyticsConfig.set("uuid", serverUUID.toString());
                analyticsConfig.save(analyticsFile);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onDisable() {
        Analytics.sendReport();
    }

    @Override
    public String name() {
        return "NexusCore";
    }

    @Override
    public String ver() {
        return "1.9.1";
    }

    @Override
    public String dlLink() {
        return "https://modrinth.com/plugin/nexuscore";
    }

    @Override
    public String prefix() {
        return prefix;
    }

    @Override
    public String lang() {
        return "https://synkdev.cc/storage/translations/lang-pld/NexusCore/lang-core.json";
    }

    @Override
    public Map<String, String> langMap() {
        return langMap;
    }
}
