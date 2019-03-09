package de.myzelyam.skychatfilter.handlers;

import de.myzelyam.skychatfilter.GeneralMessageSendEvent;
import de.myzelyam.skychatfilter.SkyChatFilter;
import de.myzelyam.skychatfilter.StaffAlerter;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AntiAdvertising extends StaffAlerter implements Listener {

    private final SkyChatFilter plugin;

    public AntiAdvertising(SkyChatFilter plugin) {
        super(plugin, "AdStaffAlerter");
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onMessage(GeneralMessageSendEvent e) {
        if (!plugin.getConfig().getStringList("EnabledModules").contains("AntiAdvertising")) return;
        ProxiedPlayer p = e.getSender();
        if (p.hasPermission("skychatfilter.bypassad") || e.isCancelled()) return;
        String text = e.getText();
        String textOrig = text;
        for (String regex : plugin.getConfig().getStringList("BlacklistRegEx")) {
            Matcher matcher = Pattern.compile(regex, Pattern.CASE_INSENSITIVE).matcher(text);

            if (matcher.find()) {
                // remove whitelisted content
                for (String regex2 : plugin.getConfig().getStringList("OverridingWhitelistRegEx")) {
                    Matcher matcher2 = Pattern.compile(regex2, Pattern.CASE_INSENSITIVE).matcher(text);
                    while (matcher2.find()) {
                        String matched = matcher2.group();
                        text = text.replace(matched, "");
                    }
                }
                // recheck blacklist
                matcher = Pattern.compile(regex, Pattern.CASE_INSENSITIVE).matcher(text);
                if (matcher.find()) {
                    // whitelist doesnt prevent it from being blacklisted => illegal
                    e.setCancelled(true);
                    p.sendMessage(plugin.getMessage("NoAdvertising", p.getServer().getInfo()));
                    alert(p, textOrig, e.getReceiver());
                    return;
                }
            }
        }

    }
}
