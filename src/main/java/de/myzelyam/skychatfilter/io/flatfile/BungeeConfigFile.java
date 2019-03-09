package de.myzelyam.skychatfilter.io.flatfile;

import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.logging.Level;

public class BungeeConfigFile implements PluginFile<Configuration> {

    private Plugin plugin;
    private String name;
    private Configuration fileConfiguration;

    public BungeeConfigFile(String name, Plugin plugin) {
        this.plugin = plugin;
        this.name = name + ".yml";
        setup();
    }

    @Override
    public String getName() {
        return name;
    }

    private void setup() {
        File file = new File(plugin.getDataFolder(), name);
        createFileIfRequired();
        try {
            fileConfiguration = ConfigurationProvider.getProvider(
                    YamlConfiguration.class).load(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void reload() {
        setup();
    }

    @Override
    public Configuration getConfig() {
        if (fileConfiguration == null) {
            reload();
        }
        return fileConfiguration;
    }

    @Override
    public void save() {
        createFileIfRequired();
    }

    private void createFileIfRequired() {
        if (!plugin.getDataFolder().exists())
            if (!plugin.getDataFolder().mkdir()) {
                plugin.getLogger().log(Level.WARNING, "Cannot create folder: " + name);
            }
        File file = new File(plugin.getDataFolder(), name);
        if (!file.exists()) {
            try {
                Files.copy(plugin.getResourceAsStream(name), file.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
