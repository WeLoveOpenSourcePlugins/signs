package dev.lukaesebrot.wlosp.signs.network;

/**
 * Represents the information about a server
 * @author Lukas Schulte Pelkum
 * @version 1.0.0
 * @since 1.0.0
 */
public class ServerInformation {

    // Define local variables
    private String name;
    private ServerStatus status;
    private int currentPlayers;
    private int maxPlayers;

    /**
     * Creates a new server information object
     * @param name The name of the server
     * @param status The status of the server
     * @param currentPlayers The current player amount
     * @param maxPlayers The maximum player amount
     */
    public ServerInformation(String name, ServerStatus status, int currentPlayers, int maxPlayers) {
        this.name = name;
        this.status = status;
        this.currentPlayers = currentPlayers;
        this.maxPlayers = maxPlayers;
    }

    /**
     * @return The name of the server
     */
    public String getName() {
        return name;
    }

    /**
     * @return The status of the server
     */
    public ServerStatus getStatus() {
        return status;
    }

    /**
     * @return The current player amount
     */
    public int getCurrentPlayers() {
        return currentPlayers;
    }

    /**
     * @return The maximum player amount
     */
    public int getMaxPlayers() {
        return maxPlayers;
    }

}
