package de.myzelyam.skychatfilter;

import com.google.common.collect.ImmutableMap;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public abstract class StaffAlerter extends Command implements Runnable {

    private final SkyChatFilter plugin;

    private final Map<Integer, MessageAction> keyMessageMap = new ConcurrentHashMap<>();

    private int nextKey = ThreadLocalRandom.current().nextInt(100000);

    public StaffAlerter(SkyChatFilter plugin) {
        super("skychatfilter-allowid", "skychatfilter.receivealert");
        this.plugin = plugin;
        plugin.getProxy().getScheduler().schedule(plugin, this, 30, 30, TimeUnit.MINUTES);
    }

    public abstract String getConfigCategory();

    public void alert(ProxiedPlayer sender, String text) {
        if (!plugin.getConfig().getString(getConfigCategory() + ".AlertMessage").equals("")) {
            for (ProxiedPlayer staff : plugin.getProxy().getPlayers()) {
                if (!staff.hasPermission("skychatfilter.receivealert")) continue;
                String pre = ChatColor.translateAlternateColorCodes('&', plugin.getConfig()
                        .getString(getConfigCategory() + ".AlertMessage").replace("{0}", sender.getName())
                        .replace("{1}", text));
                TextComponent component = new TextComponent(TextComponent.fromLegacyText(pre));
                ComponentBuilder punishments = new ComponentBuilder("");
                for (String punishment : plugin.getConfig().getSection(getConfigCategory() + ".Punishments")
                        .getKeys()) {
                    String action = plugin.getConfig().getString(getConfigCategory() + ".Punishments." +
                            punishment);
                    ClickEvent clickEvent = new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/" + action);
                    HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                            new ComponentBuilder(action.equals("<allow>") ? "Nachricht erlauben" : action)
                                    .color(ChatColor.AQUA).create());
                    punishments.append(punishment).color(ChatColor.RED).bold(true)
                            .event(clickEvent).event(hoverEvent).append(" ");
                }
                for (BaseComponent comp : punishments.create())
                    component.addExtra(comp);
                staff.sendMessage(component);
            }
        }
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length != 1 || !(sender instanceof ProxiedPlayer)) return;
        int key;
        try {
            key = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            return;
        }
        if (!keyMessageMap.containsKey(key)) return;
        boolean success = keyMessageMap.get(key).performAction((ProxiedPlayer) sender);
        if (!success) {
            sender.sendMessage(plugin.getMessage("ActionAlreadyPerformed", ((ProxiedPlayer) sender)
                    .getServer().getInfo()));
        }
    }

    private int constructNewMessageAction(String action, String message, ProxiedPlayer sender) {
        int key = nextKey++;
        MessageAction messageAction = new MessageAction(action, message, sender, plugin);
        keyMessageMap.put(key, messageAction);
        return key;
    }

    @Override
    public void run() {
        ImmutableMap.copyOf(keyMessageMap).forEach((key, action) -> {
            if (action.readyForCleanup()) {
                keyMessageMap.remove(key);
            }
        });
    }

    private static class MessageAction {
        private final SkyChatFilter plugin;
        private final String message;
        private final ProxiedPlayer sender;
        private final String action;
        private final long creationTime;
        private boolean alreadyDone = false;

        MessageAction(String action, String message, ProxiedPlayer sender, SkyChatFilter plugin) {
            this.message = message;
            this.sender = sender;
            this.plugin = plugin;
            this.action = action;
            creationTime = System.currentTimeMillis();
        }

        boolean performAction(ProxiedPlayer player) {
            if (alreadyDone) return false;
            if (action.equalsIgnoreCase("<allow>")) {
                resend();
            } else {
                execCmd(player);
            }
            alreadyDone = true;
            return true;
        }

        boolean readyForCleanup() {
            return (System.currentTimeMillis() - creationTime) > TimeUnit.MINUTES.toMillis(60);
        }

        private void execCmd(ProxiedPlayer player) {
            player.chat("/" + action);
        }

        private void resend() {
            plugin.getMessageListners().getExemptions().add(sender);
            sender.chat(message);
            plugin.getMessageListners().getExemptions().remove(sender);
        }
    }
}
