package de.myzelyam.skychatfilter;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Cancellable;
import net.md_5.bungee.api.plugin.Event;

public class GeneralMessageSendEvent extends Event implements Cancellable {

    private final ProxiedPlayer sender;
    private final boolean isPublic;
    private final ProxiedPlayer receiver;
    private String text;
    private boolean cancelled = false;

    public GeneralMessageSendEvent(ProxiedPlayer sender, String text,
                                   boolean isPublic, ProxiedPlayer receiver) {
        this.sender = sender;
        this.text = text;
        this.isPublic = isPublic;
        this.receiver = receiver;
    }

    public ProxiedPlayer getSender() {
        return sender;
    }

    /**
     * @return the receiver of the /msg or null if this is no /msg
     */
    public ProxiedPlayer getOptionalReceiver() {
        return receiver;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isPublic() {
        return isPublic;
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
