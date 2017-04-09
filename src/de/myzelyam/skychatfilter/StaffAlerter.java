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

    private static final List<String> alreadyHandledMessages = new ArrayList<>();
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

    public void alert(ProxiedPlayer sender, String text) {
        if (!plugin.getConfig().getString(getConfigCategory() + ".AlertMessage").equals("")) {
            if (alreadyHandledMessages.contains(text)) alreadyHandledMessages.remove(text);
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
                    action = action.replace("{0}", sender.getName());
                    int key = constructNewMessageAction(action, text, sender);
                    ClickEvent clickEvent =
                            new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                    "/skychatfilter-" + getConfigCategory().toLowerCase()
                                            + "-allowid " + key);
                    HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                            new ComponentBuilder(action.equals("<allow>") ? "Nachricht erlauben"
                                    : "/" + action).color(ChatColor.AQUA).create());
                    punishments.append(punishment).color(action.equals("<allow>") ? ChatColor.GREEN
                            : ChatColor.GOLD).event(clickEvent).event(hoverEvent).append(" ");
                }
                for (BaseComponent comp : punishments.create()) {
                    component.addExtra(comp);
                }
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
        keyMessageMap.get(key).performAction((ProxiedPlayer) sender);
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

        MessageAction(String action, String message, ProxiedPlayer sender, SkyChatFilter plugin) {
            this.message = message;
            this.sender = sender;
            this.plugin = plugin;
            this.action = action;
            creationTime = System.currentTimeMillis();
        }

        void performAction(ProxiedPlayer player) {
            if (alreadyHandledMessages.contains(message)) {
                sender.sendMessage(plugin.getMessage("ActionAlreadyPerformed", sender.getServer().getInfo()));
                return;
            }
            alreadyHandledMessages.add(message);
            if (action.equalsIgnoreCase("<allow>")) {
                resend();
            } else {
                execCmd(player);
            }
        }

        boolean readyForCleanup() {
            return (System.currentTimeMillis() - creationTime) > TimeUnit.MINUTES.toMillis(60);
        }

        private void execCmd(ProxiedPlayer player) {
            plugin.getProxy().getPluginManager().dispatchCommand(player, action);
        }

        private void resend() {
            plugin.getMessageListeners().getExemptions().add(sender);
            sender.chat(message);
            plugin.getMessageListeners().getExemptions().remove(sender);
        }
    }
}
