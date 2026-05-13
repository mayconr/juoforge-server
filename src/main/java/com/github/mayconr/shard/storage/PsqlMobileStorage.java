package com.github.mayconr.shard.storage;

import com.github.mayconr.juoserver.game.model.SkillValue;
import com.github.mayconr.juoserver.game.model.UOItemData;
import com.github.mayconr.juoserver.game.model.UOMobileData;
import com.github.mayconr.juoserver.infrastructure.storage.MobileStorage;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;

@Slf4j
public class PsqlMobileStorage implements MobileStorage {

    private final Executor executor;
    private final SqlSessionFactory sessionFactory;

    public PsqlMobileStorage(Executor executor, SqlSessionFactory sessionFactory) {
        this.executor = executor;
        this.sessionFactory = sessionFactory;
    }

    @Override
    public CompletableFuture<Void> deleteBySerialId(int serialId) {
        return CompletableFuture.supplyAsync(()->{
            try (var session = sessionFactory.openSession(true)) {
                int deleted = session.getMapper(MobileMapper.class).deleteBySerialId(serialId);
                session.commit();
                if (deleted == 0) {
                    log.warn("No mobile found for serialId={}", serialId);
                }
            }
            return null;
        }, executor);
    }

    @Override
    public CompletableFuture<List<UOMobileData>> findAllNpcs() {
        return CompletableFuture.supplyAsync(
                () -> {
                    try (var session = sessionFactory.openSession()) {
                        var npc = session.getMapper(MobileMapper.class).findAllNpcs();
                        return npc;
                    }
                },executor);
    }

    @Override
    public CompletableFuture<Integer> findNextMobileSerial() {
        return CompletableFuture.supplyAsync(()->{
            try (var session = sessionFactory.openSession()) {
                return session.getMapper(MobileMapper.class).findNextMobileSerial();
            }
        }, executor);
    }

    @Override
    public CompletableFuture<UOMobileData> findMobileBySerialId(int serialId) {
        return findMobileInternal(mapper->mapper.findMobileBySerialId(serialId));
    }

    @Override
    public CompletableFuture<List<SkillValue>> findSkillsBySerialId(int serialId) {
        return CompletableFuture.supplyAsync(()->{
            try (var session = sessionFactory.openSession()) {
                return session.getMapper(MobileMapper.class).findSkillsBySerialId(serialId);
            }
        }, executor);
    }

    private CompletableFuture<UOMobileData> findMobileInternal(Function<MobileMapper, UOMobileData> mapper) {
        return CompletableFuture.supplyAsync(
                () -> {
                    try (var session = sessionFactory.openSession()) {
                        final var mobileMapper = session.getMapper(MobileMapper.class);

                        final var data = mapper.apply(mobileMapper);
                        if (data != null) {
                            log.debug("Mobile [{}] loaded", data.getSerialId());
                        } else {
                            log.debug("Mobile not found");
                        }

                        return data;
                    }
                }, executor)
                .whenComplete((mobile, throwable) -> {
                    if (throwable != null) {
                        log.error(throwable.getMessage(), throwable);
                    }
                });
    }

    @Override
    public CompletableFuture<Boolean> mobileExists(String name) {
        return CompletableFuture.supplyAsync(()->{
            try (var session = sessionFactory.openSession()) {
                return session.getMapper(MobileMapper.class).mobileExists(name);
            }
        }, executor);
    }

    @Override
    public CompletableFuture<UOMobileData> saveMobileFull(int mobileSerialId, UOMobileData data, int itemSerialId, List<UOItemData> equippedItems) {
        return CompletableFuture.supplyAsync(()->{
           try (var session = sessionFactory.openSession(false)) {
               try {
                   final var mobileMapper = session.getMapper(MobileMapper.class);
                   final var itemMapper = session.getMapper(ItemMapper.class);

                   // Update serial
                   mobileMapper.updateMobileSerial(mobileSerialId);
                   itemMapper.updateItemSerial(itemSerialId);
                   mobileMapper.upsertMobile(data);

                   // Update skills
                   for (SkillValue skill : data.getSkills().values()) {
                       mobileMapper.upsertSkill(data.getSerialId(), skill);
                   }

                   for (UOItemData item : equippedItems) {
                       itemMapper.upsert(item);
                   }

                   session.commit();
                   return data;
               } catch (Exception e) {
                   session.rollback();
                   throw new RuntimeException(e);
               }
           }
        });
    }

    @Override
    public CompletableFuture<Collection<UOMobileData>> saveMobiles(int serialId, Collection<UOMobileData> mobiles, Collection<Integer> dirties) {
        return CompletableFuture.supplyAsync(()->{
            try (var session = sessionFactory.openSession(false)) {
                try {
                    final var mapper = session.getMapper(MobileMapper.class);

                    for (UOMobileData mobile : mobiles) {
                        mapper.upsertMobile(mobile);
                    }

                    mapper.updateMobileSerial(serialId);

                    for (Integer dirty : dirties) {
                        mapper.deleteBySerialId(dirty);
                    }

                    session.commit();

                    return mobiles;
                } catch (Exception e) {
                    session.rollback();
                    throw new RuntimeException(e);
                }
            }
        });
    }

    @Override
    public CompletableFuture<Void> setNextMobileSerial(int serial) {
        return null;
    }
}
