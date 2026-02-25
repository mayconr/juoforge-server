package com.github.mayconr.shard.storage;

import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.infrastructure.storage.MobileStorage;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
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
    public CompletableFuture<List<UOMobile>> findAllNpcs() {
        return CompletableFuture.supplyAsync(
                () -> {
                    try (var session = sessionFactory.openSession()) {
                        return session.getMapper(MobileMapper.class).findAllNpcs();
                    }
                });
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
    public CompletableFuture<List<AccountMobile>> findPlayersByAccount(UOAccount uoAccount) {
        return CompletableFuture.supplyAsync(
                () -> {
                    try (var session = sessionFactory.openSession()) {
                        return session.getMapper(MobileMapper.class).findAccountMobilesByAccountId(uoAccount.getId());
                    }
                }).whenComplete((accountMobiles, throwable) -> {
                    if (throwable != null) {
                        log.warn(throwable.getMessage(), throwable);
                    }
        });
    }

    @Override
    public CompletableFuture<UOMobile> findMobileById(UUID id) {
        return findMobileInternal(mapper->mapper.findMobileById(id));
    }

    @Override
    public CompletableFuture<UOMobile> findMobileBySerialId(int serialId) {
        return findMobileInternal(mapper->mapper.findMobileBySerialId(serialId));
    }

    private CompletableFuture<UOMobile> findMobileInternal(Function<MobileMapper, UOMobile> mapper) {
        return CompletableFuture.supplyAsync(
                () -> {
                    try (var session = sessionFactory.openSession()) {
                        final var mobileMapper = session.getMapper(MobileMapper.class);

                        final var mobile = mapper.apply(mobileMapper);
                        log.debug("Mobile [{}] loaded", mobile.getId());

                        var initializer = new MobileInitializer(mobileMapper);
                        initializer.initialize(mobile);

                        // TODO check reason of buffer failed without skills
                        //mobile.setSkills(loadSkills(mobile.getId()));
                        return mobile;
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
    public CompletableFuture<UOMobile> saveMobileFull(int mobileSerialId, int itemSerialId, UOMobile mobile) {
        return CompletableFuture.supplyAsync(()->{
           try (var session = sessionFactory.openSession(false)) {
               try {
                   final var mobileMapper = session.getMapper(MobileMapper.class);
                   final var itemMapper = session.getMapper(ItemMapper.class);

                   // Update serial
                   mobileMapper.updateMobileSerial(mobileSerialId);
                   itemMapper.updateItemSerial(itemSerialId);

                   // Update mobile
                   if (mobile instanceof UOPlayer player) {
                       mobileMapper.upsertPlayer(player);
                   }
                   if (mobile instanceof UONpc npc) {
                       mobileMapper.upsertNpc(npc);
                   }

                   // Update attributes
                   mobileMapper.upsertMobileAttributes(mobile);

                   // Update vitals
                   mobileMapper.upsertMobileVitals(mobile);

                   // Update runtime
                   mobileMapper.upsertMobileRuntime(mobile);

                   // Update skills
                   for (SkillValue skill : mobile.getSkills().skills()) {
                       mobileMapper.upsertSkill(mobile, skill);
                   }

                   // EquippedItems
                   for (UOItem item : mobile.getEquippedItems().values()) {
                       // Update Item
                       itemMapper.upsert(item);
                       // Update item state
                       itemMapper.upsertItemState(item);
                   }

                   session.commit();
                   return mobile;
               } catch (Exception e) {
                   session.rollback();
                   throw new RuntimeException(e);
               }
           }
        });
    }

    @Override
    public CompletableFuture<Collection<UOMobile>> saveMobiles(int serial, Collection<UOMobile> mobiles, Collection<UOMobile> dirties) {
        return CompletableFuture.supplyAsync(()->{
            try (var session = sessionFactory.openSession(false)) {
                try {
                    final var mapper = session.getMapper(MobileMapper.class);

                    for (UOMobile mobile : mobiles) {
                        if (mobile instanceof UOPlayer player) {
                            mapper.upsertPlayer(player);
                            continue;
                        }
                        if (mobile instanceof UONpc npc) {
                            mapper.upsertNpc(npc);
                        }
                    }

                    mapper.updateMobileSerial(serial);

                    for (UOMobile mobile : dirties) {
                        mapper.deleteById(mobile.getId());
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
    public CompletableFuture<Collection<UOMobile>> saveRuntime(Collection<UOMobile> mobiles) {
        return CompletableFuture.supplyAsync(()->{
            try (var session = sessionFactory.openSession(false)) {
                try {
                    final var mapper = session.getMapper(MobileMapper.class);

                    for (UOMobile mobile : mobiles) {
                        mapper.upsertMobileRuntime(mobile);
                    }
                    session.commit();

                    return mobiles;
                } catch (Exception e) {
                    session.rollback();
                    throw new RuntimeException(e);
                }
            }
        }, executor).whenComplete((e,error)->{
            if (error != null) {
                log.error(error.getMessage(), error);
            }
        });
    }

    @Override
    public CompletableFuture<Collection<UOMobile>> saveVitals(Collection<UOMobile> mobiles) {
        return CompletableFuture.supplyAsync(()->{
            try (var session = sessionFactory.openSession(false)) {
                try {
                    final var mapper = session.getMapper(MobileMapper.class);
                    for (UOMobile mobile : mobiles) {
                        mapper.upsertMobileVitals(mobile);
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
    public CompletableFuture<Collection<UOMobile>> saveAttributes(Collection<UOMobile> mobiles) {
        return CompletableFuture.supplyAsync(()->{
            try (var session = sessionFactory.openSession(false)) {
                try {
                    final var mapper = session.getMapper(MobileMapper.class);
                    for (UOMobile mobile : mobiles) {
                        mapper.upsertMobileAttributes(mobile);
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
    public CompletableFuture<Collection<UOMobile>> saveSkills(Collection<UOMobile> mobiles) {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> setNextMobileSerial(int serial) {
        return null;
    }
}
