package com.github.mayconr.juoserver.infrastructure.storage;

import com.github.mayconr.juoserver.game.model.AccountMobile;
import com.github.mayconr.juoserver.game.model.UOAccount;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.model.UONpc;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface MobileStorage {

    CompletableFuture<Integer> findNextMobileSerial();

    CompletableFuture<Void> setNextMobileSerial(int serial);

    CompletableFuture<List<AccountMobile>> findPlayersByAccount(UOAccount uoAccount);

    CompletableFuture<UOMobile> findMobileById(UUID id);

    CompletableFuture<Void> deleteBySerialId(int serialId);

    CompletableFuture<UOMobile> findMobileBySerialId(int serialId);

    CompletableFuture<Boolean> mobileExists(String name);

    CompletableFuture<List<UONpc>> findAllNpcs();

    CompletableFuture<UOMobile> saveMobileFull(int mobileSerialId, int itemSerialId, UOMobile mobile);

    CompletableFuture<Collection<UOMobile>> saveMobiles(int serial, Collection<UOMobile> mobiles, Collection<UOMobile> dirties);

    CompletableFuture<Collection<UOMobile>> saveRuntime(Collection<UOMobile> mobiles);

    CompletableFuture<Collection<UOMobile>> saveVitals(Collection<UOMobile> mobiles);

    CompletableFuture<Collection<UOMobile>> saveAttributes(Collection<UOMobile> mobiles);

    CompletableFuture<Collection<UOMobile>> saveSkills(Collection<UOMobile> mobiles);

}
