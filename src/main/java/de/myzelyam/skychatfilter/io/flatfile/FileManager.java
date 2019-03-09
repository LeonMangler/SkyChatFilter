package de.myzelyam.skychatfilter.io.flatfile;

import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;

import java.util.HashMap;
import java.util.Map;

public class FileManager {

    private Map<String, PluginFile<?>> files = new HashMap<>();

    public PluginFile<Configuration> addBungeeFile(String name, FileType type, Plugin plugin) {
        if (type == FileType.CONFIG) {
            BungeeConfigFile file = new BungeeConfigFile(name, plugin);
            files.put(name, file);
            return file;
        } else if (type == FileType.STORAGE) {
            BungeeStorageFile file = new BungeeStorageFile(name, plugin);
            files.put(name, file);
            return file;
        } else {
            throw new IllegalArgumentException("Illegal FileType " + type);
        }
    }

    public <CT> PluginFile<CT> getFile(String name) {
        //noinspection unchecked
        return (PluginFile<CT>) files.get(name);
    }

    public void reloadFile(String fileName) {
        PluginFile<?> file = files.get(fileName);
        if (file != null)
            file.reload();
        else
            throw new IllegalArgumentException("Specified file doesn't exist!");
    }

    public void reloadAll() {
        for (String fileName : files.keySet())
            reloadFile(fileName);
    }

    public void close() {
        files.clear();
    }

    public enum FileType {
        CONFIG, STORAGE
    }
}
