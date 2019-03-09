package de.myzelyam.skychatfilter;

import de.myzelyam.skychatfilter.handlers.AntiAdvertising;
import de.myzelyam.skychatfilter.handlers.AntiSpam;
import de.myzelyam.skychatfilter.handlers.AntiSwear;
import de.myzelyam.skychatfilter.handlers.CapsFilter;
import de.myzelyam.skychatfilter.io.flatfile.FileManager;
import de.myzelyam.skychatfilter.io.flatfile.PluginFile;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;

public class SkyChatFilter extends Plugin {

    private Configuration config;
    private MessageListener messageListener;
    private AntiSwear antiSwear;

    @Override
    public void onEnable() {
        PluginFile<Configuration> configFile = new FileManager()
                .addBungeeFile("config", FileManager.FileType.CONFIG, this);
        config = configFile.getConfig();

        // reload cmd
        getProxy().getPluginManager().registerCommand(this, new Command("skychatfilter",
                "skychatfilter.reload") {
            @Override
            public void execute(CommandSender sender, String[] args) {
                configFile.reload();
                config = configFile.getConfig();
                antiSwear.loadWordsFromFile();
                sender.sendMessage(ChatColor.GREEN + "Reloaded config successfully");
            }
        });
        getProxy().getPluginManager().registerListener(this, new CapsFilter(this));
        getProxy().getPluginManager().registerListener(this, new AntiSpam(this));
        getProxy().getPluginManager().registerListener(this, new AntiAdvertising(this));
        getProxy().getPluginManager().registerListener(this, antiSwear = new AntiSwear(this));
        getProxy().getPluginManager().registerListener(this, messageListener = new MessageListener(this));
    }

    public Configuration getConfig() {
        return config;
    }

    public String getMessage(String path, ServerInfo server, String... args) {
        String lang = server == null ? "" : config.getString("Language.Servers." + server.getName());
        if (lang.equals("")) lang = config.getString("Language.Servers.Default");
        String configValue = config.getString("Language." + lang + "." + path);
        if (configValue.equals("")) return ChatColor.RED + "Error: Message not found: " + path;
        String message = ChatColor.translateAlternateColorCodes('&', configValue);
        for (int i = 0; i < args.length; i++) {
            message = message.replace("{" + i + "}", args[i]);
        }
        return message;
    }

    public MessageListener getMessageListener() {
        return messageListener;
    }
}
