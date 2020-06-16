package dev.lukaesebrot.wlosp.signs.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import dev.lukaesebrot.wlosp.signs.Signs;
import dev.lukaesebrot.wlosp.signs.signs.Sign;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents the flatfile storage provider
 * @author Lukas Schulte Pelkum
 * @version 1.0.0
 * @since 1.0.0
 */
public class FlatfileStorageProvider implements StorageProvider {

    // Define local variables
    private final String fileName;
    private final Gson gson;
    private Set<Sign> signs;

    /**
     * Creates a new flatfile storage provider
     * @throws IOException An exception which Gson may throw
     */
    public FlatfileStorageProvider() throws IOException {
        // Initialize the local variables
        File file = new File(JavaPlugin.getPlugin(Signs.class).getDataFolder(), "signs.json");
        file.createNewFile();
        fileName = file.getPath();
        gson = new GsonBuilder().registerTypeAdapter(Location.class, new LocationAdapter()).create();

        // Initialize the JSON file
        JsonReader reader = new JsonReader(new FileReader(fileName));
        signs = gson.fromJson(reader, new TypeToken<Set<Sign>>(){}.getType());
    }

    @Override
    public Set<Sign> getAllSigns() {
        if (signs == null) {
            signs = new HashSet<>();
        }
        return signs;
    }

    @Override
    public void insertSign(Sign sign) {
        signs.add(sign);
        write();
    }

    @Override
    public void removeSign(Sign sign) {
        signs.remove(sign);
        write();
    }

    /**
     * Writes the current sign set to the JSON file
     */
    private void write() {
        try {
            Writer writer = new FileWriter(fileName);
            gson.toJson(signs, new TypeToken<Set<Sign>>(){}.getType(), writer);
            writer.close();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

}
