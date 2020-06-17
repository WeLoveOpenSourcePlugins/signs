package dev.lukaesebrot.wlosp.signs.signs;

import dev.lukaesebrot.wlosp.signs.Signs;
import dev.lukaesebrot.wlosp.signs.network.ProxyCommunicationAdapter;
import dev.lukaesebrot.wlosp.signs.storage.StorageProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

/**
 * Keeps track of every sign and their state
 * @author Lukas Schulte Pelkum
 * @version 1.0.0
 * @since 1.0.0
 */
public class SignRegistry {

    // Define local variables
    private final StorageProvider storageProvider;
    private final ProxyCommunicationAdapter proxyCommunicationAdapter;
    private Set<Sign> signs;

    /**
     * Creates a new sign registry
     * @param storageProvider The storage provider to use
     */
    public SignRegistry(StorageProvider storageProvider, ProxyCommunicationAdapter proxyCommunicationAdapter) {
        this.storageProvider = storageProvider;
        this.proxyCommunicationAdapter = proxyCommunicationAdapter;
        this.signs = ConcurrentHashMap.newKeySet();
    }

    /**
     * Loads and registers all signs from the given storage provider
     */
    public void registerInitialSigns() {
        this.signs = storageProvider.getAllSigns();
    }

    /**
     * Registers a new sign
     * @param sign The sign to register
     */
    public void registerSign(Sign sign) {
        // Return if the sign is already registered
        if (signs.contains(sign)) {
            return;
        }

        // Save the sign to the storage
        storageProvider.insertSign(sign);

        // Add the sign to the current registered signs
        signs.add(sign);
    }

    /**
     * Removes a sign
     * @param sign The sign to remove
     */
    public void removeSign(Sign sign) {
        // Return if the sign is not registered
        if (!signs.contains(sign)) {
            return;
        }

        // Delete the sign from the storage
        storageProvider.removeSign(sign);

        // Remove the sign from the current registered signs
        signs.remove(sign);
    }

    /**
     * Returns the sign matching the given filter
     * @param filter The filter to use for the sign
     * @return The optional sign
     */
    public Optional<Sign> getSign(Predicate<Sign> filter) {
        return signs.stream().filter(filter).findFirst();
    }

    /**
     * @return The current set of signs
     */
    public Set<Sign> getAllSigns() {
        return signs;
    }

    /**
     * Schedules the updating sequence of all signs
     */
    public void scheduleSignUpdates() {
        Signs plugin = JavaPlugin.getPlugin(Signs.class);
        new SignUpdateSequence(this, proxyCommunicationAdapter).runTaskTimerAsynchronously(plugin, 0L, plugin.getConfig().getLong("signs.updateInterval"));
    }

}
