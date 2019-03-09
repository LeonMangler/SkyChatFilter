package de.myzelyam.skychatfilter.io.flatfile;

public interface PluginFile<CT> {

    String getName();

    void reload();

    CT getConfig();

    void save();
}
