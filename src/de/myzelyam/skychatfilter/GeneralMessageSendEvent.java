package de.myzelyam.skychatfilter;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Cancellable;
import net.md_5.bungee.api.plugin.Event;

public class GeneralMessageSendEvent extends Event implements Cancellable {

    private final ProxiedPlayer sender, receiver;
    private String text;
    private boolean cancelled = false;

    public GeneralMessageSendEvent(ProxiedPlayer sender, String text) {
        this.sender = sender;
        this.text = text;
        this.receiver = null;
    }

    public GeneralMessageSendEvent(ProxiedPlayer sender, String message, ProxiedPlayer receiver) {
        this.sender = sender;
        this.text = message;
        this.receiver = receiver;
    }

    public ProxiedPlayer getSender() {
        return sender;
    }

    /**
     * @return the receiver of the personal message or null if this is no pm
     */
    public ProxiedPlayer getReceiver() {
        return receiver;
    }

    public boolean isCommand() {
        return text.startsWith("/");
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
