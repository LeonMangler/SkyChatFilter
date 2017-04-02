package de.myzelyam.skychatfilter;

import net.md_5.bungee.api.connection.ProxiedPlayer;

public interface Filter {

    boolean isPermitted(ProxiedPlayer sender, String text);
}
