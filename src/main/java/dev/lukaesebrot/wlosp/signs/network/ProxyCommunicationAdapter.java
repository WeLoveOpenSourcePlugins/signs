package dev.lukaesebrot.wlosp.signs.network;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import dev.lukaesebrot.wlosp.signs.Signs;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.*;
import java.util.function.Consumer;

/**
 * Helps to communicate with the proxy by providing a simple interface to work with plugin messages
 * @author Lukas Schulte Pelkum
 * @version 1.0.0
 * @since 1.0.0
 */
public class ProxyCommunicationAdapter implements PluginMessageListener {

    // Define the local socket
    private Socket socket;

    // Define the waiting queue
    private final List<Consumer<IncomingPluginMessageContext>> waiting;

    /**
     * Creates a new proxy communication adapter
     */
    public ProxyCommunicationAdapter() {
        this.waiting = Collections.synchronizedList(new ArrayList<>());
    }

    /**
     * Opens the local socket
     * @throws IOException An exception the socket may throw
     */
    public void openSocket() throws IOException {
        socket = new Socket();
        socket.setSoTimeout(4000);
    }

    /**
     * Closes the local socket
     * @throws IOException An exception the socket may throw
     */
    public void closeSocket() throws IOException {
        if (socket != null) {
            socket.close();
        }
    }

    /**
     * Sends a player to another server
     * @param player The player to send
     * @param server The server to send the player to
     */
    public void sendPlayerToServer(Player player, String server) {
        // Define the output stream
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Connect");
        out.writeUTF(server);

        // Send the plugin message
        player.sendPluginMessage(JavaPlugin.getPlugin(Signs.class), "BungeeCord", out.toByteArray());
    }

    /**
     * Retrieves some information about another server
     * @param player The player to send the message from
     * @param server The server to retrieve the information from
     * @param callback Gets called when the information were received
     */
    public void retrieveServerInformation(Player player, String server, Consumer<ServerInformation> callback) {
        // Retrieve the server's IP
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("ServerIP");
        out.writeUTF(server);
        player.sendPluginMessage(JavaPlugin.getPlugin(Signs.class), "BungeeCord", out.toByteArray());

        // Ping the server after the IP was received
        awaitResponse(context -> {
            // Read the server information
            ByteArrayDataInput in = ByteStreams.newDataInput(context.getMessage());
            in.readUTF();
            in.readUTF();
            String ip = in.readUTF();
            int port = in.readUnsignedShort();

            // Ping the server
            try (Socket socket = new Socket()) {
                // Set up the socket
                socket.connect(new InetSocketAddress(ip, port), 20);

                // Define the output and input streams
                DataOutputStream sockOut = new DataOutputStream(socket.getOutputStream());
                DataInputStream socketIn = new DataInputStream(socket.getInputStream());
                sockOut.write(0xFE);

                // Build the response string
                int current;
                StringBuilder stringBuffer = new StringBuilder();
                while ((current = socketIn.read()) != -1) {
                    if (current > 16 && current != 255 && current != 23 && current != 24) {
                        stringBuffer.append((char) current);
                    }
                }

                // Define the response and accept the callback
                String[] response = stringBuffer.toString().split("§");
                int currentPlayers = Integer.parseInt(response[1]);
                int maxPlayers = Integer.parseInt(response[2]);
                callback.accept(new ServerInformation(server, currentPlayers != maxPlayers ? ServerStatus.ONLINE : ServerStatus.FULL, currentPlayers, maxPlayers));
            } catch (IOException exception) {
                callback.accept(new ServerInformation(server, ServerStatus.OFFLINE, 0, 0));
            }
        });
    }

    /**
     * Adds the given consumer to the list of waiting responses
     * @param callback Gets called when the next response arrives
     */
    public void awaitResponse(Consumer<IncomingPluginMessageContext> callback) {
        waiting.add(callback);
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] bytes) {
        // Check if the channel is valid
        if (!channel.equals("BungeeCord")) {
            return;
        }

        // Retrieve the first waiting callback consumer and accept it
        if (waiting.isEmpty()) {
            return;
        }
        waiting.get(0).accept(new IncomingPluginMessageContext(player, bytes));
        waiting.remove(0);
    }

}
