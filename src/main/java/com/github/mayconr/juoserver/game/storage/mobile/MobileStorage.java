package com.github.mayconr.juoserver.game.storage.mobile;

import com.github.mayconr.juoserver.game.core.model.AccountLoginMobile;
import com.github.mayconr.juoserver.game.core.model.UOAccount;
import com.github.mayconr.juoserver.game.core.model.UOMobile;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface MobileStorage {
    CompletableFuture<List<AccountLoginMobile>> findPlayersByAccount(UOAccount uoAccount);

    CompletableFuture<Optional<UOMobile>> findMobileById(UUID id);

    CompletableFuture<Optional<UOMobile>> findMobileBySerialId(int serialId);

    CompletableFuture<Boolean> mobileExists(String name);

    CompletableFuture<UOMobile> saveMobileFull(UOMobile mobile);

    CompletableFuture<Collection<UOMobile>> saveMobiles(Collection<UOMobile> mobiles);

    CompletableFuture<Collection<UOMobile>> saveRuntime(Collection<UOMobile> mobiles);

    CompletableFuture<Collection<UOMobile>> saveVitals(Collection<UOMobile> mobiles);

    CompletableFuture<Collection<UOMobile>> saveAttributes(Collection<UOMobile> mobiles);

}
