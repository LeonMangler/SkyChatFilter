package de.myzelyam.skychatfilter.handlers;

import de.myzelyam.skychatfilter.GeneralMessageSendEvent;

import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

public class CapsFilter implements Listener {

    @EventHandler(priority = EventPriority.LOW)
    public void onMessage(GeneralMessageSendEvent e) {
        String text = e.getText();
        int amountOfUppercase = 0;
        for (char c : text.toCharArray()) {
            if (Character.isUpperCase(c)) amountOfUppercase++;
        }
        if (text.length() > 3 && amountOfUppercase >= (text.length() / 2)
                && !e.getSender().hasPermission("skychatfilter.bypasscaps")) {
            String newText = text.toLowerCase();
            newText = Character.toUpperCase(newText.charAt(0)) + newText.substring(1, newText.length());
            e.setText(newText);
        }
    }
}
