package dev.lukaesebrot.wlosp.signs.storage;

import dev.lukaesebrot.wlosp.signs.signs.Sign;

import java.util.Set;

/**
 * Represents a general storage provider
 * @author Lukas Schulte Pelkum
 * @version 1.0.0
 * @since 1.0.0
 */
public interface StorageProvider {

    Set<Sign> getAllSigns();

    void insertSign(Sign sign);

    void removeSign(Sign sign);

}
