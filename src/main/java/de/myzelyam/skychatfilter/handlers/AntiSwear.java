package de.myzelyam.skychatfilter.handlers;

import de.myzelyam.skychatfilter.GeneralMessageSendEvent;
import de.myzelyam.skychatfilter.SkyChatFilter;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AntiSwear implements Listener {

    private final SkyChatFilter plugin;

    private List<String> swearWords = new ArrayList<>();

    public AntiSwear(SkyChatFilter plugin) {
        this.plugin = plugin;
        loadWordsFromFile();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onMessage(GeneralMessageSendEvent e) {
        if (!plugin.getConfig().getStringList("EnabledModules").contains("AntiSwear"))return;
        String text = e.getText().toLowerCase(Locale.ENGLISH);
        ProxiedPlayer p = e.getSender();
        if (p.hasPermission("skychatfilter.bypassswear") || e.isCancelled()) return;
        for (String badWord : swearWords) {
            if (text.contains(badWord.toLowerCase(Locale.ENGLISH))) {
                e.setCancelled(true);
                p.sendMessage(plugin.getMessage("NoSwearing", p.getServer().getInfo()));
                break;
            }
        }
    }

    public void loadWordsFromFile() {
        swearWords.clear();
        File file = new File(plugin.getDataFolder(), "blacklist.txt");
        try {
            file.createNewFile();
            swearWords.addAll(Files.readAllLines(file.toPath()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
