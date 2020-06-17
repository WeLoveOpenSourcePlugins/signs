package dev.lukaesebrot.wlosp.signs.signs;

import dev.lukaesebrot.wlosp.signs.Signs;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Handles the {@link BlockBreakEvent} for server signs
 * @author Lukas Schulte Pelkum
 * @version 1.0.0
 * @since 1.0.0
 */
public class SignBreakListener implements Listener {

    // Define local variables
    private final Signs plugin;
    private final SignRegistry signRegistry;

    /**
     * Creates a new sign break listener
     * @param signRegistry The sign registry to use
     */
    public SignBreakListener(SignRegistry signRegistry) {
        this.plugin = JavaPlugin.getPlugin(Signs.class);
        this.signRegistry = signRegistry;
    }

    @EventHandler
    public void handleSignBreak(BlockBreakEvent event) {
        signRegistry.getSign(sign -> sign.locationEquals(event.getBlock().getLocation())).ifPresent(sign -> {
            // Check if the player has the required permission
            Player player = event.getPlayer();
            Configuration config = plugin.getConfig();
            if (!player.hasPermission("signs.use")) {
                player.sendMessage(config.getString("messages.prefix") + config.getString("messages.insufficientPermissions"));
                event.setCancelled(true);
                return;
            }

            // Unregister the sign
            signRegistry.removeSign(sign);
            player.sendMessage(config.getString("messages.prefix") + config.getString("messages.signSuccessfullyRemoved"));
        });
    }

}
