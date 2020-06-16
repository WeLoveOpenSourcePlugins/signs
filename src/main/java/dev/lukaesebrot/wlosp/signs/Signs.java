package dev.lukaesebrot.wlosp.signs;

import dev.lukaesebrot.wlosp.signs.network.ProxyCommunicationAdapter;
import dev.lukaesebrot.wlosp.signs.signs.Sign;
import dev.lukaesebrot.wlosp.signs.signs.SignClickListener;
import dev.lukaesebrot.wlosp.signs.signs.SignRegistry;
import dev.lukaesebrot.wlosp.signs.storage.FlatfileStorageProvider;
import dev.lukaesebrot.wlosp.signs.storage.StorageProvider;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;

/**
 * The loading class of the 'signs' plugin
 * @author Lukas Schulte Pelkum
 * @version 1.0.0
 * @since 1.0.0
 */
public class Signs extends JavaPlugin {

    @Override
    public void onEnable() {
        // Initialize the default configuration
        saveDefaultConfig();

        // Initialize the storage provider
        // TODO: Implement more storage providers
        StorageProvider storageProvider;
        try {
            storageProvider = new FlatfileStorageProvider();
        } catch (IOException exception) {
            exception.printStackTrace();
            getPluginLoader().disablePlugin(this);
            return;
        }

        // Initialize the proxy communication adapter and the sign registry
        ProxyCommunicationAdapter proxyCommunicationAdapter = new ProxyCommunicationAdapter();
        SignRegistry signRegistry = new SignRegistry(storageProvider, proxyCommunicationAdapter);
        signRegistry.registerInitialSigns();
        signRegistry.scheduleSignUpdates();

        // Insert a dummy sign
        // TODO: Remove this
        signRegistry.registerSign(new Sign(Bukkit.getWorld("world").getBlockAt(159, 64, 2).getLocation(), "server"));

        // Register the sign click listener
        getServer().getPluginManager().registerEvents(new SignClickListener(signRegistry, proxyCommunicationAdapter), this);

        // Register the BungeeCord plugin channel
        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", proxyCommunicationAdapter);
    }

}
