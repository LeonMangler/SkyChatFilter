package de.myzelyam.skychatfilter.handlers;

import de.myzelyam.skychatfilter.GeneralMessageSendEvent;
import de.myzelyam.skychatfilter.SkyChatFilter;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

public class AntiSwear implements Listener {

    private final SkyChatFilter plugin;

    public AntiSwear(SkyChatFilter plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onMessage(GeneralMessageSendEvent e) {
        String text = e.getText();
        ProxiedPlayer p = e.getSender();
        if (p.hasPermission("skychatfilter.bypassswear") || e.isCancelled()) return;

    }
}
