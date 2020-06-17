package dev.lukaesebrot.wlosp.signs.signs;

import dev.lukaesebrot.wlosp.signs.Signs;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Handles the 'createSign' command
 * @author Lukas Schulte Pelkum
 * @version 1.0.0
 * @since 1.0.0
 */
public class CreateSignCommand implements CommandExecutor {

    // Define local variables
    private final Signs plugin;
    private final SignRegistry signRegistry;

    /**
     * Creates a new sign command executor
     * @param signRegistry The sign registry to use
     */
    public CreateSignCommand(SignRegistry signRegistry) {
        this.plugin = JavaPlugin.getPlugin(Signs.class);
        this.signRegistry = signRegistry;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Check if the sender is a player
        Configuration config = plugin.getConfig();
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command is restricted to players.");
            return true;
        }

        // Check if the player has the required permission
        Player player = (Player) sender;
        if (!player.hasPermission("signs.use")) {
            player.sendMessage(config.getString("messages.prefix") + config.getString("messages.insufficientPermissions"));
            return true;
        }

        // Check if an argument was provided
        if (args.length == 0) {
            player.sendMessage(config.getString("messages.prefix") + config.getString("messages.invalidUsage"));
            return true;
        }

        // Get the block the player looks at and check if it is a sign
        Block targetBlock = player.getTargetBlock(null, 5);
        if (!(targetBlock.getState() instanceof org.bukkit.block.Sign)) {
            player.sendMessage(config.getString("messages.prefix") + config.getString("messages.noSign"));
            return true;
        }

        // Check if the sign is already registered and register it if it isn't
        signRegistry.getSign(sign -> sign.locationEquals(targetBlock.getLocation())).ifPresentOrElse(sign ->
            player.sendMessage(config.getString("messages.prefix") + config.getString("messages.signAlreadyRegistered"))
        , () -> {
            signRegistry.registerSign(new Sign(targetBlock.getLocation(), args[0]));
            player.sendMessage(config.getString("messages.prefix") + config.getString("messages.signSuccessfullyRegistered"));
        });
        return true;
    }

}
