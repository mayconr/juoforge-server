package com.github.mayconr.juoserver.infrastructure.storage;

import com.github.mayconr.juoserver.game.model.SkillValue;
import com.github.mayconr.juoserver.game.model.UOItemData;
import com.github.mayconr.juoserver.game.model.UOMobileData;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface MobileStorage {

    CompletableFuture<Integer> findNextMobileSerial();

    CompletableFuture<Void> setNextMobileSerial(int serial);

    CompletableFuture<Void> deleteBySerialId(int serialId);

    CompletableFuture<UOMobileData> findMobileBySerialId(int serialId);

    CompletableFuture<List<SkillValue>> findSkillsBySerialId(int serialId);

    CompletableFuture<Boolean> mobileExists(String name);

    CompletableFuture<List<UOMobileData>> findAllNpcs();

    CompletableFuture<UOMobileData> saveMobileFull(int mobileSerialId, UOMobileData data, int itemSerialId, List<UOItemData> equippedItems);

    CompletableFuture<Collection<UOMobileData>> saveMobiles(int serial, Collection<UOMobileData> mobiles, Collection<Integer> dirties);

}
