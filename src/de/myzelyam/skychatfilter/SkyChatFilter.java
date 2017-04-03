package de.myzelyam.skychatfilter;

import de.myzelyam.skychatfilter.handlers.CapsFilter;

import net.md_5.bungee.api.plugin.Plugin;

public class SkyChatFilter extends Plugin {

    @Override
    public void onEnable() {
        getProxy().getPluginManager().registerListener(this, new MessageListeners(this));

        // handlers
        getProxy().getPluginManager().registerListener(this, new CapsFilter());
    }
}
