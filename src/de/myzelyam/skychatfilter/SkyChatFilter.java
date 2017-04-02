package de.myzelyam.skychatfilter;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;

public class SkyChatFilter extends Plugin {

    @Override
    public void onEnable() {
        getProxy().getPluginManager().registerListener(this, new MessageListeners(this));
    }

    public boolean allowChatMessage(ProxiedPlayer sender, String text) {
        return true;
    }
}
