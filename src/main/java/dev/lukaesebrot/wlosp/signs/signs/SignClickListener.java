package dev.lukaesebrot.wlosp.signs.signs;

import dev.lukaesebrot.wlosp.signs.Signs;
import dev.lukaesebrot.wlosp.signs.network.ProxyCommunicationAdapter;
import org.bukkit.block.Block;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Represents the listener that gets registered to handle sign interactions
 * @author Lukas Schulte Pelkum
 * @version 1.0.0
 * @since 1.0.0
 */
public class SignClickListener implements Listener {

    // Define local variables
    private final Signs plugin;
    private final SignRegistry signRegistry;
    private final ProxyCommunicationAdapter proxyCommunicationAdapter;

    /**
     * Creates a new sign click listener
     * @param signRegistry The sign registry to use
     * @param proxyCommunicationAdapter The proxy communication adapter to use
     */
    public SignClickListener(SignRegistry signRegistry, ProxyCommunicationAdapter proxyCommunicationAdapter) {
        this.plugin = JavaPlugin.getPlugin(Signs.class);
        this.signRegistry = signRegistry;
        this.proxyCommunicationAdapter = proxyCommunicationAdapter;
    }

    @EventHandler
    public void handleSignClick(PlayerInteractEvent event) {
        // Check if the action is a right click on a block
        if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            return;
        }

        // Check if the block is a sign
        Block block = event.getClickedBlock();
        if (block == null || !(block.getState() instanceof org.bukkit.block.Sign)) {
            return;
        }

        // Notify the transportation controller about a new sign interaction if the sign is registered
        signRegistry.getSign(sign -> sign.locationEquals(block.getLocation())).ifPresent(sign -> {
            Player player = event.getPlayer();
            Configuration config = plugin.getConfig();
            proxyCommunicationAdapter.retrieveServerInformation(player, sign.getServer(), information -> {
                switch (information.getStatus()) {
                    case ONLINE:
                        proxyCommunicationAdapter.sendPlayerToServer(event.getPlayer(), sign.getServer());
                        break;
                    case OFFLINE:
                        player.sendMessage(config.getString("messages.prefix") + config.getString("messages.serverOffline"));
                        break;
                    case FULL:
                        player.sendMessage(config.getString("messages.prefix") + config.getString("messages.serverFull"));
                }
                event.setCancelled(true);
            });
        });
    }

}
