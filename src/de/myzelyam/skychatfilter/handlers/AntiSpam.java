package de.myzelyam.skychatfilter.handlers;

import de.myzelyam.skychatfilter.GeneralMessageSendEvent;
import de.myzelyam.skychatfilter.SkyChatFilter;

import eu.mrgames.mrcore.util.StringUtils;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class AntiSpam implements Listener, Runnable {

    private final Map<UUID, Integer> playerMessagesSentMap = new ConcurrentHashMap<>();
    private final Map<UUID, Long> playerLoginTimeMap = new ConcurrentHashMap<>();
    private final Map<UUID, LinkedList<Message>> playerLastMessagesMap = new ConcurrentHashMap<>();

    private final SkyChatFilter plugin;

    public AntiSpam(SkyChatFilter plugin) {
        this.plugin = plugin;
        plugin.getProxy().getScheduler().schedule(plugin, this,
                plugin.getConfig().getInt("FloodClearPeriodInSeconds"), TimeUnit.SECONDS);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onMessage(GeneralMessageSendEvent e) {
        String text = e.getText();
        ProxiedPlayer p = e.getSender();
        if (p.hasPermission("skychatfilter.bypassspam") || e.isCancelled()) return;

        // check amount <=> time
        int allowedMax = ((System.currentTimeMillis() - playerLoginTimeMap.get(p.getUniqueId()))
                > TimeUnit.SECONDS.toMillis(plugin.getConfig().getInt("FloodTimeUntilNoMoreJoinInSeconds")))
                ? plugin.getConfig().getInt("FloodMaxMessagesPerPeriodWithoutJoin")
                : plugin.getConfig().getInt("FloodMaxMessagesPerPeriodAfterJoin");
        playerMessagesSentMap.put(p.getUniqueId(), playerMessagesSentMap.get(p.getUniqueId()) + 1);
        if (playerMessagesSentMap.get(p.getUniqueId()) >=
                (allowedMax + plugin.getConfig().getInt("FloodKickAfterXOverMax"))) {
            p.disconnect(plugin.getMessage("AntiSpamTriggered", p.getServer().getInfo()));
            return;
        }
        if (playerMessagesSentMap.get(p.getUniqueId()) >= allowedMax) {
            e.setCancelled(true);
            p.sendMessage(plugin.getMessage("AntiSpamTriggered", p.getServer().getInfo()));
            return;
            // check similarity to last messages
        } else for (Message message : playerLastMessagesMap.get(p.getUniqueId())) {
            if (!message.isAllowed(text)) {
                e.setCancelled(true);
                p.sendMessage(plugin.getMessage("TextTooSimilar", p.getServer().getInfo()));
                break;
            }
        }

        if (!playerLastMessagesMap.containsKey(p.getUniqueId())) {
            playerLastMessagesMap.put(p.getUniqueId(), new LinkedList<>());
        }
        LinkedList<Message> lastMessages = playerLastMessagesMap.get(p.getUniqueId());
        lastMessages.add(new Message(text));
        if (lastMessages.size() > plugin.getConfig().getInt("SameMessageSaveAmount"))
            lastMessages.removeFirst();
    }


    @EventHandler
    public void onJoin(PostLoginEvent e) {
        playerLoginTimeMap.put(e.getPlayer().getUniqueId(), System.currentTimeMillis());
    }

    @Override
    public void run() {
        for (UUID player : new HashSet<>(playerMessagesSentMap.keySet())) {
            if (plugin.getProxy().getPlayer(player) == null)
                playerMessagesSentMap.remove(player);
            else
                playerMessagesSentMap.put(player, 0);
        }
        for (UUID player : new HashSet<>(playerLoginTimeMap.keySet())) {
            if (plugin.getProxy().getPlayer(player) == null)
                playerLoginTimeMap.remove(player);
        }
        for (UUID player : new HashSet<>(playerLastMessagesMap.keySet())) {
            if (plugin.getProxy().getPlayer(player) == null)
                playerLastMessagesMap.remove(player);
        }
    }

    private class Message {
        private final String text;
        private final long time;

        public Message(String text) {
            this.text = text;
            this.time = System.currentTimeMillis();
        }

        public String getText() {
            return text;
        }

        public long getSendTime() {
            return time;
        }

        boolean isAllowed(String text) {
            double similarity = StringUtils.similarity(text, this.text);
            if ((similarity >= plugin.getConfig().getDouble("SameMessageSimilarity")
                    || text.startsWith(this.text))
                    && text.length() > plugin.getConfig().getInt("SameMessageMinLength")) {
                return !((System.currentTimeMillis() - time) < TimeUnit.SECONDS.toMillis(plugin.getConfig()
                        .getInt("SameMessageExpiryInSeconds")));
            }
            return true;
        }
    }
}
