package de.myzelyam.skychatfilter;

import com.google.common.collect.Sets;

import de.myzelyam.skymessage.PrivateMessageEvent;

import eu.mrgames.mrcore.util.StringUtils;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.Set;

public class MessageListener implements Listener {

    private final SkyChatFilter plugin;
    private final Set<ProxiedPlayer> exemptions = Sets.newConcurrentHashSet();

    public MessageListener(SkyChatFilter plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPublicMessageSend(ChatEvent e) {
        if (!(e.getSender() instanceof ProxiedPlayer)) return;
        ProxiedPlayer player = (ProxiedPlayer) e.getSender();
        String message = e.getMessage();
        if (e.isCommand()) {
            String label = message.contains(" ") ? message.split(" ")[0] : message;
            label = label.substring(1, label.length()).toLowerCase();
            if (!StringUtils.containsIgnoreCase(plugin.getConfig().getStringList("IncludedCommands"), label))
                return;
        }
        if (exemptions.contains(player)) return;
        GeneralMessageSendEvent event = new GeneralMessageSendEvent(player, message);
        plugin.getProxy().getPluginManager().callEvent(event);
        e.setMessage(event.getText());
        if (event.isCancelled()) e.setCancelled(true);
    }

    @EventHandler
    public void onPrivateMessageSend(PrivateMessageEvent e) {
        ProxiedPlayer player = e.getSender();
        String message = e.getMessage();
        if (exemptions.contains(player)) return;
        GeneralMessageSendEvent event = new GeneralMessageSendEvent(player, message, e.getReceiver());
        plugin.getProxy().getPluginManager().callEvent(event);
        e.setMessage(event.getText());
        if (event.isCancelled()) e.setCancelled(true);
    }

    public Set<ProxiedPlayer> getExemptions() {
        return exemptions;
    }
}
