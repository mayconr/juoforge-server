package com.github.mayconr.shard.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.infrastructure.storage.DataNotFoundException;
import com.github.mayconr.juoserver.infrastructure.storage.MobileStorage;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Slf4j
public class PsqlMobileStorage extends AbstractStorage implements MobileStorage {

    private static final String MOBILE_EXISTS = """
            SELECT EXISTS (
                SELECT 1
                FROM mobiles
                WHERE LOWER(name) = LOWER(?)
            );
            """;

    private final Executor executor;
    private final SaveMobileFull saveMobileFull;
    private final GetSerial getSerial;
    private final SaveMobileRuntime saveMobileRuntime;
    private final SaveMobileVitals saveMobileVitals;
    private final SaveMobileAttributes saveMobileAttributes;
    private final SaveMobiles saveMobiles;
    private final SqlSessionFactory sessionFactory;

    public PsqlMobileStorage(DataSource dataSource, Executor executor, ObjectMapper objectMapper, SqlSessionFactory sessionFactory) {
        super(dataSource);
        this.executor = executor;
        this.saveMobileFull = new SaveMobileFull(dataSource, executor, objectMapper);
        this.getSerial = new GetSerial(dataSource, executor);
        this.saveMobileRuntime = new SaveMobileRuntime(dataSource, executor, objectMapper);
        this.saveMobileVitals = new SaveMobileVitals(dataSource, executor);
        this.saveMobileAttributes = new SaveMobileAttributes(dataSource, executor);
        this.saveMobiles = new SaveMobiles(dataSource, executor);
        this.sessionFactory = sessionFactory;
    }

    @Override
    public CompletableFuture<List<UOMobile>> loadNPCs() {
        return CompletableFuture.supplyAsync(
                () -> {
                    final var sql = "SELECT * FROM v_mobile_full WHERE account_id is null;";

                    return findMany(sql, p -> {}, this::mapMobileData);
                });
    }

    @Override
    public CompletableFuture<Integer> getNextMobileSerial() {
        return CompletableFuture.supplyAsync(()->{
            try (var session = sessionFactory.openSession()) {
                return session.getMapper(MobileSqlMapper.class).getNextMobileSerial();
            }
        }, executor);
    }

    @Override
    public CompletableFuture<Void> setNextMobileSerial(int serial) {
        return null;
    }

    @Override
    public CompletableFuture<List<AccountLoginMobile>> findPlayersByAccount(UOAccount uoAccount) {
        return CompletableFuture.supplyAsync(
                () -> {
                    final var sql = "SELECT * FROM v_account_mobiles_login WHERE account_id = ?;";

                    return findMany(sql, p -> p.setObject(1, uoAccount.getId()), this::mapLoginMobile);
                });
    }

    private AccountLoginMobile mapLoginMobile(ResultSet resultSet) throws SQLException {
        return new AccountLoginMobile(
                resultSet.getInt("serial_id"), resultSet.getString("mobile_name"));
    }

    @Override
    public CompletableFuture<UOMobile> findMobileById(UUID id) {
        return CompletableFuture.supplyAsync(
                () -> {
                    final var mobileSql = "SELECT * FROM v_mobile_full WHERE mobile_id = ?;";
                    final var mobile = findOne(mobileSql, p -> p.setObject(1, id), this::mapMobileData)
                            .orElseThrow(()->new DataNotFoundException("Mobile not found for id "+id));
                    mobile.setSkills(loadSkills(id));
                    return mobile;
                },
                executor);
    }

    @Override
    public CompletableFuture<UOMobile> findMobileBySerialId(int serialId) {
        return CompletableFuture.supplyAsync(
                () -> {
                    final var sql = "SELECT * FROM v_mobile_full WHERE serial_id = ?;";
                    final var mobile = findOne(sql, p -> p.setInt(1, serialId), this::mapMobileData)
                            .orElseThrow(()->new DataNotFoundException("Mobile not found for serial "+serialId));
                    mobile.setSkills(loadSkills(mobile.getId()));
                    return mobile;
                },
                executor);
    }

    private SkillContainer loadSkills(UUID mobileId) {
        final var skillSql = "SELECT * FROM mobile_skills WHERE mobile_id = ?";
        final var skills = findMany(skillSql, ps -> ps.setObject(1, mobileId), MobileMapper::mapMobileSkills);
        return new SkillContainer(skills);
    }

    private UOMobile mapMobileData(ResultSet rs) throws SQLException {
        final var accountId = rs.getObject("account_id", UUID.class);
        UOMobile mobile;
        if (accountId == null) {
            mobile = new UONpc(MobileMapper.mapMobile(rs), NpcType.MOUNT);
        } else {
            mobile = new UOPlayer(MobileMapper.mapMobile(rs), accountId);
        }
        return mobile;
    }

    @Override
    public CompletableFuture<UOMobile> saveMobileFull(int mobileSerialId, int itemSerialId, UOMobile mobile) {
        return saveMobileFull.saveMobileFull(mobileSerialId, itemSerialId, mobile);
    }

    @Override
    public CompletableFuture<Collection<UOMobile>> saveMobiles(int serial, Collection<UOMobile> mobiles, Collection<UOMobile> dirties) {
        return saveMobiles.saveMobiles(serial, mobiles, dirties);
    }

    @Override
    public CompletableFuture<Collection<UOMobile>> saveRuntime(Collection<UOMobile> mobiles) {
        return saveMobileRuntime.saveRuntime(mobiles);
    }

    @Override
    public CompletableFuture<Collection<UOMobile>> saveVitals(Collection<UOMobile> mobiles) {
        return saveMobileVitals.saveVitals(mobiles);
    }

    @Override
    public CompletableFuture<Collection<UOMobile>> saveAttributes(Collection<UOMobile> mobiles) {
        return saveMobileAttributes.saveAttributes(mobiles);
    }

    @Override
    public CompletableFuture<Boolean> mobileExists(String name) {
        return CompletableFuture.supplyAsync(()->{
            try (var session = sessionFactory.openSession()) {
                return session.getMapper(MobileSqlMapper.class).mobileExists(name);
            }
        }, executor);
    }

    @Override
    public CompletableFuture<Collection<UOMobile>> saveSkills(Collection<UOMobile> mobiles) {
        return CompletableFuture.completedFuture(null);
    }
}
