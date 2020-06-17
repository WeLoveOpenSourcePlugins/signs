package dev.lukaesebrot.wlosp.signs;

import dev.lukaesebrot.wlosp.signs.network.ProxyCommunicationAdapter;
import dev.lukaesebrot.wlosp.signs.signs.*;
import dev.lukaesebrot.wlosp.signs.storage.FlatfileStorageProvider;
import dev.lukaesebrot.wlosp.signs.storage.StorageProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;

/**
 * The loading class of the 'signs' plugin
 * @author Lukas Schulte Pelkum
 * @version 1.0.0
 * @since 1.0.0
 */
public class Signs extends JavaPlugin {

    // Define the proxy communication adapter
    private ProxyCommunicationAdapter proxyCommunicationAdapter;

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

        // Initialize the proxy communication adapter and open the local socket
        proxyCommunicationAdapter = new ProxyCommunicationAdapter();
        try {
            proxyCommunicationAdapter.openSocket();
        } catch (IOException exception) {
            exception.printStackTrace();
            getPluginLoader().disablePlugin(this);
            return;
        }


        // Initialize the proxy communication adapter and the sign registry
        SignRegistry signRegistry = new SignRegistry(storageProvider, proxyCommunicationAdapter);
        signRegistry.registerInitialSigns();
        signRegistry.scheduleSignUpdates();

        // Register the sign creation command
        getCommand("createSign").setExecutor(new CreateSignCommand(signRegistry));

        // Register the sign click and break listener
        getServer().getPluginManager().registerEvents(new SignClickListener(signRegistry, proxyCommunicationAdapter), this);
        getServer().getPluginManager().registerEvents(new SignBreakListener(signRegistry), this);

        // Register the BungeeCord plugin channel
        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", proxyCommunicationAdapter);
    }

    @Override
    public void onDisable() {
        // Close the local socket
        try {
            proxyCommunicationAdapter.closeSocket();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

}
