package de.myzelyam.skychatfilter;

import de.myzelyam.skymessage.PrivateMessageEvent;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class MessageListeners implements Listener {
    private final SkyChatFilter plugin;

    public MessageListeners(SkyChatFilter plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPublicMessageSend(ChatEvent e) {
        if (!(e.getSender() instanceof ProxiedPlayer)) return;
        ProxiedPlayer player = (ProxiedPlayer) e.getSender();
        String message = e.getMessage();
        if (e.isCommand()) return;
        GeneralMessageSendEvent event = new GeneralMessageSendEvent(player, message, true);
        plugin.getProxy().getPluginManager().callEvent(event);
        e.setMessage(event.getText());
        if (event.isCancelled()) e.setCancelled(true);
    }

    @EventHandler
    public void onPrivateMessageSend(PrivateMessageEvent e) {
        ProxiedPlayer player = e.getSender();
        String message = e.getMessage();
        GeneralMessageSendEvent event = new GeneralMessageSendEvent(player, message, false);
        plugin.getProxy().getPluginManager().callEvent(event);
        e.setMessage(event.getText());
        if (event.isCancelled()) e.setCancelled(true);
    }
}
