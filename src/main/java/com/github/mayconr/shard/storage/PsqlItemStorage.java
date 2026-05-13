package com.github.mayconr.shard.storage;

import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.UOItemData;
import com.github.mayconr.juoserver.infrastructure.storage.ItemStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;

@RequiredArgsConstructor
@Slf4j
public class PsqlItemStorage implements ItemStorage {

    private final Executor executor;
    private final SqlSessionFactory sessionFactory;

    @Override
    public CompletableFuture<Integer> findNextItemSerial() {
        return CompletableFuture.supplyAsync(()->{
            try (var session = sessionFactory.openSession()) {
                return session.getMapper(ItemMapper.class).findNextItemSerial();
            }
        }, executor);
    }

    @Override
    public CompletableFuture<List<UOItemData>> findAllEquippedItems(int mobileSerialId) {
        return findInternal(mapper->mapper.findAllEquippedItems(mobileSerialId));
    }

    @Override
    public CompletableFuture<List<UOItemData>> findAllGroundItems() {
        return findInternal(ItemMapper::findAllGroundItems);
    }

    @Override
    public CompletableFuture<List<UOItemData>> loadContainerItems(int containerSerialId) {
        return findInternal(mapper->mapper.findAllContainerItems(containerSerialId));
    }

    @Override
    public CompletableFuture<UOItemData> findItemBySerialId(int itemSerialId) {
        return findInternal(mapper->mapper.findItemBySerialId(itemSerialId));
    }

    private <T> CompletableFuture<T> findInternal(Function<ItemMapper, T> function) {
        return CompletableFuture.supplyAsync(()->{
            try (var session = sessionFactory.openSession()) {
                final var mapper = session.getMapper(ItemMapper.class);
                return function.apply(mapper);
            }
        }, executor);
    }

    @Override
    public CompletableFuture<UOItem> saveItemFull(UOItem item) {
        return CompletableFuture.supplyAsync(()->{
            try (var session = sessionFactory.openSession(false)) {
                try {
                    final var mapper = session.getMapper(ItemMapper.class);
                    mapper.upsert(item.toData());
                    mapper.upsertItemState(item.toData());
                    session.commit();
                    return item;
                } catch (Exception e) {
                    session.rollback();
                    throw new RuntimeException(e);
                }
            }
        });
    }

    @Override
    public CompletableFuture<Collection<UOItemData>> saveItems(int currentSerialId, Collection<UOItemData> items, Collection<UOItemData> dirties) {
        return CompletableFuture.supplyAsync(()->{
            try (var session = sessionFactory.openSession(false)) {
                try {
                    final var itemMapper = session.getMapper(ItemMapper.class);

                    for (UOItemData itemData : items) {
                        itemMapper.upsert(itemData);
                    }

                    itemMapper.updateItemSerial(currentSerialId);

                    for (UOItemData dirty : dirties) {
                        itemMapper.deleteBySerialId(dirty.getSerialId());
                    }

                    session.commit();

                    return items;
                } catch (Exception e) {
                    session.rollback();
                    throw new RuntimeException(e);
                }
            }
        }, executor);
    }

}
