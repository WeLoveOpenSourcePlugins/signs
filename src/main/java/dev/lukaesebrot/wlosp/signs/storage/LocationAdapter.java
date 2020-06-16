package dev.lukaesebrot.wlosp.signs.storage;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.io.IOException;

/**
 * Represents an Gson type adapter for Bukkit locations
 * @author Lukas Schulte Pelkum
 * @version 1.0.0
 * @since 1.0.0
 */
public class LocationAdapter extends TypeAdapter<Location> {

    @Override
    public void write(JsonWriter jsonWriter, Location location) throws IOException {
        jsonWriter.beginObject()
                .name("world").value(location.getWorld().getName())
                .name("x").value(location.getBlockX())
                .name("y").value(location.getBlockY())
                .name("z").value(location.getBlockZ())
        .endObject();
    }

    @Override
    public Location read(JsonReader jsonReader) throws IOException {
        JsonObject object = new JsonParser().parse(jsonReader).getAsJsonObject();
        return new Location(
                Bukkit.getWorld(object.get("world").getAsString()),
                object.get("x").getAsInt(),
                object.get("y").getAsInt(),
                object.get("z").getAsInt()
        );
    }

}
