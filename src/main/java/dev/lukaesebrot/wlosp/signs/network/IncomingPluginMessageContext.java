package dev.lukaesebrot.wlosp.signs.network;

import org.bukkit.entity.Player;

/**
 * Represents the data of an incoming plugin message
 * @author Lukas Schulte Pelkum
 * @version 1.0.0
 * @since 1.0.0
 */
public class IncomingPluginMessageContext {

    // Define local variables
    private Player player;
    private byte[] message;

    /**
     * Creates a new incoming plugin message context
     * @param player The player involved in this plugin message
     * @param message The data of the plugin message
     */
    public IncomingPluginMessageContext(Player player, byte[] message) {
        this.player = player;
        this.message = message;
    }

    /**
     * @return The player involved in this plugin message
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * @return The data of the plugin message
     */
    public byte[] getMessage() {
        return message;
    }

}
