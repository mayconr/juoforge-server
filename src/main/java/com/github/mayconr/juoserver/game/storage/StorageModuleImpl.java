package com.github.mayconr.juoserver.game.storage;

import com.github.mayconr.juoserver.game.model.Location;
import com.github.mayconr.juoserver.game.model.UOAccount;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;
import lombok.RequiredArgsConstructor;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

@RequiredArgsConstructor
public class StorageModuleImpl implements StorageModule {
    private final RealmStorage storage;

    @Override
    public List<UOMobile> getMobilesInRange(Location location, int radius, Predicate<UOMobile> filter) {
        return storage.getMobilesInRange(location, radius, filter);
    }

    @Override
    public Optional<UOMobile> getMobileBySerialId(int serial) {
        return storage.getMobile(serial);
    }

    @Override
    public CompletableFuture<UOAccount> getAccountByUsername(String username) {
        return storage.getAccountByUsername(username);
    }
}
