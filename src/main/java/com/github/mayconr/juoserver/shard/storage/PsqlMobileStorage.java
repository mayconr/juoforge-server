package com.github.mayconr.juoserver.shard.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.infrastructure.storage.MobileStorage;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
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
    private final InsertMobileFull insertMobileFull;
    private final GetSerial getSerial;
    private final SaveMobileRuntime saveMobileRuntime;
    private final SaveMobileVitals saveMobileVitals;
    private final SaveMobileAttributes saveMobileAttributes;
    private final SaveMobiles saveMobiles;

    public PsqlMobileStorage(DataSource dataSource, Executor executor, ObjectMapper objectMapper) {
        super(dataSource);
        this.executor = executor;
        this.insertMobileFull = new InsertMobileFull(dataSource, executor, objectMapper);
        this.getSerial = new GetSerial(dataSource, executor);
        this.saveMobileRuntime = new SaveMobileRuntime(dataSource, executor, objectMapper);
        this.saveMobileVitals = new SaveMobileVitals(dataSource, executor);
        this.saveMobileAttributes = new SaveMobileAttributes(dataSource, executor);
        this.saveMobiles = new SaveMobiles(dataSource, executor);
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
        return getSerial.getNextSerial("MOBILE");
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
    public CompletableFuture<Optional<UOMobile>> findMobileById(UUID id) {
        return CompletableFuture.supplyAsync(
                () -> {
                    final var sql = "SELECT * FROM v_mobile_full WHERE mobile_id = ?;";

                    return findOne(sql, p -> p.setObject(1, id), this::mapMobileData);
                },
                executor);
    }

    @Override
    public CompletableFuture<Optional<UOMobile>> findMobileBySerialId(int serialId) {
        return CompletableFuture.supplyAsync(
                () -> {
                    final var sql = "SELECT * FROM v_mobile_full WHERE serial_id = ?;";

                    return findOne(sql, p -> p.setInt(1, serialId), this::mapMobileData);
                },
                executor);
    }

    private UOMobile mapMobileData(ResultSet rs) throws SQLException {
        final var accountId = rs.getObject("account_id", UUID.class);
        UOMobile mobile;
        if (accountId == null) {
            final var mountItemName = rs.getString("mount_item_name");
            mobile = new UONpc(MobileMapper.map(rs), NpcType.MOUNT,"BANKER", mountItemName);
        } else {
            mobile = new UOPlayer(MobileMapper.map(rs), accountId);
        }
        return mobile;
    }

    @Override
    public CompletableFuture<UOMobile> saveMobileFull(UOMobile mobile) {
        return insertMobileFull.saveMobileFull(mobile);
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
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement ps = conn.prepareStatement(MOBILE_EXISTS)) {
                 ps.setString(1, name);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getBoolean(1);
                    }
                    return false;
                }
            } catch (SQLException exception) {
                throw new RuntimeException("Error executing query", exception);
            }
        }, executor);
    }
}
