package com.github.mayconr.juoserver.game.storage.mobile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import com.github.mayconr.juoserver.game.core.model.*;

public interface MobileStorage {
    CompletableFuture<List<AccountLoginMobile>> findPlayersByAccount(UOAccount uoAccount);

    CompletableFuture<Optional<UOMobile>> findMobileById(UUID id);

    CompletableFuture<Optional<UOMobile>> findMobileBySerialId(int serialId);

    CompletableFuture<UOPlayer> createNewPlayer(PlayerDetails details);
}
