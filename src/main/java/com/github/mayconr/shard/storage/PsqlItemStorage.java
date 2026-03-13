package com.github.mayconr.shard.storage;

import com.github.mayconr.juoserver.game.model.Container;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.UOMobile;
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
    public CompletableFuture<List<UOItem>> findAllEquippedItems(UOMobile mobile) {
        return findInternal(mapper->mapper.findAllEquippedItems(mobile.getId()));
    }

    @Override
    public CompletableFuture<List<UOItem>> findAllGroundItems() {
        return findInternal(ItemMapper::findAllGroundItems);
    }

    @Override
    public CompletableFuture<List<UOItem>> loadContainerItems(Container container) {
        return findInternal(mapper->mapper.findAllContainerItems(container.getId()));
    }

    @Override
    public CompletableFuture<UOItem> findItemBySerialId(int serialId) {
        return findInternal(mapper->mapper.findItemBySerialId(serialId));
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
                    mapper.upsert(item);
                    mapper.upsertItemState(item);
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
    public CompletableFuture<Collection<UOItem>> saveItems(int serial, Collection<UOItem> items, Collection<UOItem> dirties) {
        try (var session = sessionFactory.openSession(false)) {
            try {
                final var itemMapper = session.getMapper(ItemMapper.class);

                for (UOItem item : items) {
                    itemMapper.upsert(item);
                }

                itemMapper.updateItemSerial(serial);

                for (UOItem dirty : dirties) {
                    itemMapper.deleteById(dirty.getId());
                }

                session.commit();

                return CompletableFuture.completedFuture(items);
            } catch (Exception e) {
                session.rollback();
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public CompletableFuture<Collection<UOItem>> saveStates(Collection<UOItem> items) {
        try (var session = sessionFactory.openSession(true)) {
            try {
                for (UOItem item : items) {
                    session.getMapper(ItemMapper.class).upsertItemState(item);
                }
                session.commit();
                return CompletableFuture.completedFuture(items);
            } catch (Exception e) {
                session.rollback();
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public CompletableFuture<Void> setNextItemSerial(int serial) {
        return null;
    }
}
