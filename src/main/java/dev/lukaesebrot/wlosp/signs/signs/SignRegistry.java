package dev.lukaesebrot.wlosp.signs.signs;

import com.google.common.collect.Iterables;
import dev.lukaesebrot.wlosp.signs.Signs;
import dev.lukaesebrot.wlosp.signs.network.ProxyCommunicationAdapter;
import dev.lukaesebrot.wlosp.signs.storage.StorageProvider;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
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
     * Returns the corresponding sign
     * @param filter The filter to use for the sign
     * @return The optional sign
     */
    public Optional<Sign> getSign(Predicate<Sign> filter) {
        return signs.stream().filter(filter).findFirst();
    }

    /**
     * Schedules the updating sequence of all signs
     */
    public void scheduleSignUpdates() {
        Signs plugin = JavaPlugin.getPlugin(Signs.class);
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, task -> {
            // TODO: Remove debug message
            System.out.println("CALL ME MAYBE");
            signs.forEach(sign -> {
                // Define the block and its state
                Block block = sign.getLocation().getBlock();
                BlockState state;
                try {
                    state = Bukkit.getScheduler().callSyncMethod(plugin, block::getState).get();
                } catch (Exception exception) {
                    exception.printStackTrace();
                    return;
                }

                // Check if the block is a sign
                if (!(state instanceof org.bukkit.block.Sign)) {
                    removeSign(sign);
                    return;
                }
                org.bukkit.block.Sign signState = (org.bukkit.block.Sign) state;

                // Define the mounting point of the sign
                BlockData data = state.getBlockData();
                BlockFace mountingFace = data instanceof Directional ? ((Directional) data).getFacing().getOppositeFace() : BlockFace.DOWN;
                Block mountingPoint = block.getRelative(mountingFace);

                // Update the surrounding
                Player player = Iterables.getFirst(Bukkit.getOnlinePlayers(), null);
                if (player == null) {
                    return;
                }
                proxyCommunicationAdapter.retrieveServerInformation(player, sign.getServer(), information -> {
                    // Update the lines of the sign
                    String status = information.getStatus().toString().toLowerCase();
                    String colorCode = "§" + ChatColor.valueOf(plugin.getConfig().getString("signs.states." + status + ".color").toUpperCase()).getChar();
                    List<String> lineModel = plugin.getConfig().getStringList("signs.lineModel");
                    AtomicInteger counter = new AtomicInteger(0);
                    lineModel.forEach(line -> {
                        // Define the current index and replace the placeholders
                        int current = counter.getAndIncrement();
                        lineModel.set(current, line.replace("{server_name}", information.getName())
                                .replace("{player_count}", String.valueOf(information.getCurrentPlayers()))
                                .replace("{max_player_count}", String.valueOf(information.getMaxPlayers()))
                                .replace("{status_color_code}", colorCode)
                                .replace("{status}", status)
                        );

                        // Set the line of the sign
                        if (current < 4) {
                            signState.setLine(current, lineModel.get(current));
                        }
                    });
                    signState.update();

                    // Update the mounting point of the sign
                    Material material = Material.valueOf(plugin.getConfig().getString("signs.states." + status + ".material").toUpperCase());
                    mountingPoint.setType(material);
                });
            });
        }, 1, plugin.getConfig().getInt("signs.updateInterval"));
    }

}
