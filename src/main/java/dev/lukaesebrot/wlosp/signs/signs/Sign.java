package dev.lukaesebrot.wlosp.signs.signs;

import org.bukkit.Location;

/**
 * Represents a sign
 * @author Lukas Schulte Pelkum
 * @version 1.0.0
 * @since 1.0.0
 */
public class Sign {

    // Define model variables
    private final Location location;
    private final String server;

    /**
     * Creates a new sign object
     * @param location The location of the sign
     * @param server The server name of the sign
     */
    public Sign(Location location, String server) {
        this.location = location;
        this.server = server;
    }

    /**
     * @return The location of the sign
     */
    public Location getLocation() {
        return location;
    }

    /**
     * Checks whether or not the location of the current sign is equal to the given one
     * @param other The location to check
     * @return Whether or not the locations match
     */
    public boolean locationEquals(Location other) {
        return other.getWorld() != null
                && other.getBlockX() == location.getBlockX()
                && other.getBlockY() == location.getBlockY()
                && other.getBlockZ() == location.getBlockZ()
                && other.getWorld().equals(location.getWorld());
    }

    /**
     * @return The server name of the sign
     */
    public String getServer() {
        return server;
    }

    @Override
    public boolean equals(Object obj) {
        // Check of the given object is a sign
        if (!(obj instanceof Sign)) {
            return false;
        }

        // Define the sign object
        Sign sign = (Sign) obj;

        // Return the final result
        return locationEquals(sign.getLocation()) && sign.getServer().equals(server);
    }

}
