package cc.synkdev.nexusCore.bukkit.objects;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

@Getter @Setter @AllArgsConstructor
public class PluginData {
    private String name;
    private String version;
    private Map<String, Integer> commandUses;
    public PluginData(JavaPlugin plugin) {
        this.name = plugin.getDescription().getName();
        this.version = plugin.getDescription().getVersion();
        this.commandUses = new HashMap<>();
    }
}
