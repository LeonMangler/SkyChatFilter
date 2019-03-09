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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public abstract class StaffAlerter extends Command implements Runnable {

    private final List<String> alreadyHandledMessages = new ArrayList<>();
    private final SkyChatFilter plugin;
    private final Map<Integer, MessageAction> keyMessageMap = new ConcurrentHashMap<>();
    private final String configCategory;
    private int nextKey = ThreadLocalRandom.current().nextInt(100000);

    public StaffAlerter(SkyChatFilter plugin, String configCategory) {
        super("skychatfilter-" + configCategory.toLowerCase() + "-allowid", "skychatfilter.receivealert");
        this.plugin = plugin;
        this.configCategory = configCategory;
        plugin.getProxy().getScheduler().schedule(plugin, this, 30, 30, TimeUnit.MINUTES);
        plugin.getProxy().getPluginManager().registerCommand(plugin, this);
    }

    public String getConfigCategory() {
        return configCategory;
    }

    public SkyChatFilter getPlugin() {
        return plugin;
    }

    public void alert(ProxiedPlayer sender, String text, ProxiedPlayer optionalReceiver) {
        if (!plugin.getConfig().getString(getConfigCategory() + ".AlertMessage").equals("")) {
            alreadyHandledMessages.remove(text);
            String playerInfo = optionalReceiver == null ? sender.getName() : sender.getName() + " -> "
                    + optionalReceiver.getName();
            String pre = ChatColor.translateAlternateColorCodes('&', plugin.getConfig()
                    .getString(getConfigCategory() + ".AlertMessage").replace("{0}", playerInfo)
                    .replace("{1}", text));
            TextComponent component = new TextComponent(TextComponent.fromLegacyText(pre));
            ComponentBuilder punishments = new ComponentBuilder("");
            for (String punishment : plugin.getConfig().getSection(getConfigCategory() + ".Punishments")
                    .getKeys()) {
                String action = plugin.getConfig().getString(getConfigCategory() + ".Punishments." +
                        punishment);
                action = action.replace("{0}", sender.getName());
                int key = constructNewMessageAction(action, optionalReceiver == null ? text
                        : ("/msg " + optionalReceiver.getName() + " " + text), sender);
                ClickEvent clickEvent =
                        new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                "/skychatfilter-" + getConfigCategory().toLowerCase()
                                        + "-allowid " + key);
                String allowMessage = plugin.getMessage("AllowMessage", sender.getServer().getInfo());
                HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        new ComponentBuilder(action.equals("<allow>") ? allowMessage
                                : "/" + action).color(ChatColor.AQUA).create());
                punishments.append(punishment).color(action.equals("<allow>") ? ChatColor.GREEN
                        : ChatColor.GOLD).event(clickEvent).event(hoverEvent).append(" ");
            }
            for (BaseComponent comp : punishments.create()) {
                component.addExtra(comp);
            }
            for (ProxiedPlayer staff : plugin.getProxy().getPlayers()) {
                if (!staff.hasPermission("skychatfilter.receivealert")) continue;
                staff.sendMessage(component);
            }
        }
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length != 1 || !(sender instanceof ProxiedPlayer)) {
            sender.sendMessage(ChatColor.RED
                    + "This command shouldnt be used manually! (failed args or player check)");
            return;
        }
        int key;
        try {
            key = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "This command shouldnt be used manually! (" + args[0] + " is" +
                    " not a number)");
            return;
        }
        if (!keyMessageMap.containsKey(key)) {
            sender.sendMessage(ChatColor.RED + "Action timed out or never existed");
            return;
        }
        keyMessageMap.get(key).performAction((ProxiedPlayer) sender);
    }

    private int constructNewMessageAction(String action, String message, ProxiedPlayer sender) {
        int key = nextKey++;
        MessageAction messageAction = new MessageAction(action, message, sender, this);
        keyMessageMap.put(key, messageAction);
        return key;
    }

    @Override
    public void run() {
        ImmutableMap.copyOf(keyMessageMap).forEach((key, action) -> {
            if (action.readyForCleanup()) {
                alreadyHandledMessages.remove(action.message);
                keyMessageMap.remove(key);
            }
        });
    }

    private static class MessageAction {
        private final StaffAlerter alerter;
        private final String message;
        private final ProxiedPlayer sender;
        private final String action;
        private final long creationTime;

        MessageAction(String action, String message, ProxiedPlayer sender, StaffAlerter alerter) {
            this.message = message;
            this.sender = sender;
            this.alerter = alerter;
            this.action = action;
            creationTime = System.currentTimeMillis();
        }

        void performAction(ProxiedPlayer player) {
            if (alerter.alreadyHandledMessages.contains(message)) {
                player.sendMessage(alerter.getPlugin().getMessage("ActionAlreadyPerformed", player
                        .getServer().getInfo()));
                return;
            }
            alerter.alreadyHandledMessages.add(message);
            if (action.equalsIgnoreCase("<allow>")) {
                resend();
                sender.sendMessage(alerter.getPlugin().getMessage("MessageAllowed",
                        sender.getServer().getInfo()));
            } else {
                execCmd(player);
            }
        }

        boolean readyForCleanup() {
            return (System.currentTimeMillis() - creationTime) > TimeUnit.MINUTES.toMillis(60);
        }

        private void execCmd(ProxiedPlayer player) {
            alerter.getPlugin().getProxy().getPluginManager().dispatchCommand(player, action);
        }

        private void resend() {
            alerter.getPlugin().getMessageListener().getExemptions().add(sender);
            if (!message.startsWith("/")) {
                sender.chat(message);
                alerter.getPlugin().getMessageListener().getExemptions().remove(sender);
            } else {
                try {
                    alerter.getPlugin().getProxy().getPluginManager().dispatchCommand(sender, message);
                } finally {
                    alerter.getPlugin().getProxy().getScheduler().schedule(alerter.getPlugin(), () ->
                                    alerter.getPlugin().getMessageListener().getExemptions().remove(sender),
                            1, TimeUnit.SECONDS);
                }
            }
        }
    }
}
