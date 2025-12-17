package cc.synkdev.nexusCore.components;

import lombok.Getter;

import java.io.File;

@Getter
public class PluginUpdate {
    private String num;
    private String plugin;
    private String dl;
    private File current;
    public PluginUpdate(String num, String plugin, String dl, File current) {
        this.num = num;
        this.plugin = plugin;
        this.dl = dl;
        this.current = current;
    }
}
