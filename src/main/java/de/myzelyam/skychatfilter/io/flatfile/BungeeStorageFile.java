package de.myzelyam.skychatfilter.io.flatfile;

import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

public class BungeeStorageFile implements PluginFile<Configuration> {

    private Plugin plugin;
    private String name;
    private Configuration fileConfiguration;

    public BungeeStorageFile(String name, Plugin plugin) {
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
        try {
            if (!file.exists()) {
                if (!file.createNewFile()) {
                    plugin.getLogger().log(Level.WARNING, "Cannot create file: " + name);
                }
            }
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
        return fileConfiguration;
    }

    @Override
    public void save() {
        try {
            ConfigurationProvider.getProvider(YamlConfiguration.class)
                    .save(fileConfiguration, new File(plugin.getDataFolder(), name));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
