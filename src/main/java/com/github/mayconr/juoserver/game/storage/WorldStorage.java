package com.github.mayconr.juoserver.game.storage;

import com.github.mayconr.juoserver.game.model.Location;
import com.github.mayconr.juoserver.game.model.UOAccount;
import com.github.mayconr.juoserver.game.model.UOMobile;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

/** Storage queries available through the world's configured storage. */
public interface WorldStorage {
    List<UOMobile> getMobilesInRange(Location location, int radius, Predicate<UOMobile> filter);

    Optional<UOMobile> getMobileBySerialId(int serial);

    CompletableFuture<UOAccount> getAccountByUsername(String username);
}
