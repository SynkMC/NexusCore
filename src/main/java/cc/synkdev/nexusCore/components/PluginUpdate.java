package cc.synkdev.nexusCore.components;

import lombok.Getter;

import java.io.File;

@Getter
public class PluginUpdate {
    private final String num;
    private final String plugin;
    private final String dl;
    private final File current;
    public PluginUpdate(String num, String plugin, String dl, File current) {
        this.num = num;
        this.plugin = plugin;
        this.dl = dl;
        this.current = current;
    }
}
