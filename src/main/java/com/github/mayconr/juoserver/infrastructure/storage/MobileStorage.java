package com.github.mayconr.juoserver.infrastructure.storage;

import com.github.mayconr.juoserver.game.model.AccountLoginMobile;
import com.github.mayconr.juoserver.game.model.UOAccount;
import com.github.mayconr.juoserver.game.model.UOMobile;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface MobileStorage {

    CompletableFuture<Integer> getNextMobileSerial();

    CompletableFuture<Void> setNextMobileSerial(int serial);

    CompletableFuture<List<AccountLoginMobile>> findPlayersByAccount(UOAccount uoAccount);

    CompletableFuture<Optional<UOMobile>> findMobileById(UUID id);

    CompletableFuture<Optional<UOMobile>> findMobileBySerialId(int serialId);

    CompletableFuture<Boolean> mobileExists(String name);

    CompletableFuture<List<UOMobile>> loadNPCs();

    /*
        PERSISTENCE METHODS
     */

    CompletableFuture<UOMobile> saveMobileFull(UOMobile mobile);

    CompletableFuture<Collection<UOMobile>> saveMobiles(int serial, Collection<UOMobile> mobiles, Collection<UOMobile> dirties);

    CompletableFuture<Collection<UOMobile>> saveRuntime(Collection<UOMobile> mobiles);

    CompletableFuture<Collection<UOMobile>> saveVitals(Collection<UOMobile> mobiles);

    CompletableFuture<Collection<UOMobile>> saveAttributes(Collection<UOMobile> mobiles);

}
