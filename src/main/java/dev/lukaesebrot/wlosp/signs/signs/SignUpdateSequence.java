package dev.lukaesebrot.wlosp.signs.signs;

import com.google.common.collect.Iterables;
import dev.lukaesebrot.wlosp.signs.Signs;
import dev.lukaesebrot.wlosp.signs.network.ProxyCommunicationAdapter;
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
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Represents the sequence that gets scheduled to update all signs
 * @author Lukas Schulte Pelkum
 * @version 1.0.0
 * @since 1.0.0
 */
public class SignUpdateSequence extends BukkitRunnable {

    // Define local variables
    private final Signs plugin;
    private final SignRegistry signRegistry;
    private final ProxyCommunicationAdapter proxyCommunicationAdapter;

    /**
     * Creates a new sign update sequence
     * @param signRegistry The sign registry to use
     * @param proxyCommunicationAdapter The proxy communication adapter to use
     */
    public SignUpdateSequence(SignRegistry signRegistry, ProxyCommunicationAdapter proxyCommunicationAdapter) {
        this.plugin = JavaPlugin.getPlugin(Signs.class);
        this.signRegistry = signRegistry;
        this.proxyCommunicationAdapter = proxyCommunicationAdapter;
    }

    @Override
    public void run() {
        signRegistry.getAllSigns().forEach(sign -> {
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
                signRegistry.removeSign(sign);
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
    }

}
