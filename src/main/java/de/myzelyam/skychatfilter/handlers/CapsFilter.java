package de.myzelyam.skychatfilter.handlers;

import de.myzelyam.skychatfilter.GeneralMessageSendEvent;
import de.myzelyam.skychatfilter.SkyChatFilter;

import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import java.util.Locale;

public class CapsFilter implements Listener {

    private final SkyChatFilter plugin;

    public CapsFilter(SkyChatFilter plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onMessage(GeneralMessageSendEvent e) {
        if (!plugin.getConfig().getStringList("EnabledModules").contains("CapsFilter")) return;
        String text = e.getText();
        int amountOfUppercase = 0;
        for (char c : text.toCharArray()) {
            if (Character.isUpperCase(c)) amountOfUppercase++;
        }
        double capsPercentage = (double) amountOfUppercase / (double) text.length();
        if (text.length() >= plugin.getConfig().getInt("CapsMinLength")
                && capsPercentage >= plugin.getConfig().getDouble("CapsMinPercentage")
                && !e.getSender().hasPermission("skychatfilter.bypasscaps")) {
            String newText = text.toLowerCase(Locale.ENGLISH);
            newText = Character.toUpperCase(newText.charAt(0)) + newText.substring(1);
            if (plugin.getConfig().getBoolean("CapsAdjustMessage"))
                e.setText(newText);
            else e.setCancelled(true);
            e.getSender().sendMessage(plugin.getMessage("AntiCaps", e.getSender().getServer().getInfo()));
        }
    }
}
