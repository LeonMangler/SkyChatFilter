package de.myzelyam.skychatfilter.filters;

import de.myzelyam.skychatfilter.Filter;

import net.md_5.bungee.api.connection.ProxiedPlayer;

public class CapsFilter implements Filter {

    @Override
    public boolean isPermitted(ProxiedPlayer sender, String text) {
        return true;
    }
}
