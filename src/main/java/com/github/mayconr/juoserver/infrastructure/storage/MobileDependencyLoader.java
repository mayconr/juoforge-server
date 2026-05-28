package com.github.mayconr.juoserver.infrastructure.storage;

import com.github.mayconr.juoserver.game.model.SkillContainer;
import com.github.mayconr.juoserver.game.model.UOMobile;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class MobileDependencyLoader {
    private final MobileStorage mobileStorage;
    private final ItemStorage itemStorage;
    private final ItemCache itemCache;
    private final ItemMapper itemMapper;

    private CompletableFuture<UOMobile> loadMobileSkills(UOMobile mobile) {
        return mobileStorage.findSkillsBySerialId(mobile.getSerialId()).thenApply(skills -> {
            mobile.setSkills(new SkillContainer(skills));
            return mobile;
        });
    }

    private CompletableFuture<UOMobile> loadEquippedItems(UOMobile mobile) {
        return itemStorage.findAllEquippedItems(mobile.getSerialId())
                .thenApply(itemMapper::mapToItem)
                .thenApply(itemCache::putAll)
                .thenApply(items -> {
                    items.forEach(mobile::equipItem);
                    return mobile;
                });
    }

    public CompletableFuture<UOMobile> loadDependencies(UOMobile mobile) {
        return loadMobileSkills(mobile)
                .thenCompose(this::loadEquippedItems);
    }
}
