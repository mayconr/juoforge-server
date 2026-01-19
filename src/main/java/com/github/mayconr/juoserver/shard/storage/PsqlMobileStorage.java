package com.github.mayconr.juoserver.shard.storage;

import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.infrastructure.storage.mobile.MobileStorage;
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

    private static final String UPDATE_MOBILE_RUNTIME = """          
           UPDATE mobile_runtime
           SET
               x          = ?,
               y          = ?,
               z          = ?,
               direction  = ?,
               running    = ?,
               hitpoints  = ?,
               stamina    = ?,
               mana       = ?,
               updated_at = NOW()
           WHERE mobile_id = ?;
           """;

    private static final String UPDATE_MOBILE_VITALS = """
            UPDATE mobile_vitals
            SET
               max_hitpoints = ?,
               max_stamina   = ?,
               max_mana      = ?
            WHERE mobile_id = ?;
            """;

    private static final String UPDATE_MOBILE_ATTRIBUTES = """
            UPDATE mobile_attributes
            SET
               strength = ?,
               dexterity = ?,
               intelligence = ?,
               stat_cap = ?,
               followers = ?,
               max_followers = ?,
               luck = ?,
               tithing_points = ?
            WHERE mobile_id = ?;
            """;

    private static final String UPDATE_MOBILES = """
            INSERT INTO mobiles (
                            id,
                            serial_id,
                            account_id,
                            name,
                            model_id,
                            hue,
                            race,
                            gender,
                            notoriety,
                            status
                        )
                        VALUES (
                            ?, ?, ?, ?, ?, ?, ?, ?, ?, ?
                        )
                        ON CONFLICT (id) DO UPDATE
                        SET
                            name       = EXCLUDED.name,
                            model_id  = EXCLUDED.model_id,
                            hue       = EXCLUDED.hue,
                            race      = EXCLUDED.race,
                            gender    = EXCLUDED.gender,
                            notoriety = EXCLUDED.notoriety,
                            status    = EXCLUDED.status;
            """;

    private static final String INSERT_MOBILE_FULL = """
            WITH new_mobile AS (
                INSERT INTO mobiles (
                    id, account_id, name,
                    model_id, hue, race, gender,
                    notoriety, status
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                RETURNING id, serial_id
            ),
            attrs AS (
                INSERT INTO mobile_attributes (
                    mobile_id, strength, dexterity, intelligence,
                    stat_cap, followers, max_followers
                )
                SELECT id, 10, 10, 10, 225, 0, 5
                FROM new_mobile
            ),
            vitals AS (
                INSERT INTO mobile_vitals (
                    mobile_id, max_hitpoints, max_stamina, max_mana
                )
                SELECT id, 50, 50, 50
                FROM new_mobile
            )
            INSERT INTO mobile_runtime (
                mobile_id, x, y, z,
                direction, running,
                hitpoints, stamina, mana
            )
            SELECT
                id,
                ?, ?, ?,        -- spawn x,y,z
                0,
                false,
                50, 50, 50
            FROM new_mobile
            RETURNING
                (SELECT serial_id FROM new_mobile) AS serial_id;
            """;

    private static final String MOBILE_EXISTS = """
            SELECT EXISTS (
                SELECT 1
                FROM mobiles
                WHERE LOWER(name) = LOWER(?)
            );
            """;

    private final Executor executor;

    public PsqlMobileStorage(DataSource dataSource, Executor executor) {
        super(dataSource);
        this.executor = executor;
    }

    @Override
    public CompletableFuture<List<AccountLoginMobile>> findPlayersByAccount(UOAccount uoAccount) {
        return CompletableFuture.supplyAsync(
                () -> {
                    final var sql = "SELECT * FROM v_account_mobiles_login WHERE account_id = ?;";

                    return findMany(
                            sql, p -> p.setObject(1, uoAccount.getId()), this::mapLoginMobile);
                });
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

    private AccountLoginMobile mapLoginMobile(ResultSet resultSet) throws SQLException {
        return new AccountLoginMobile(
                resultSet.getInt("serial_id"), resultSet.getString("mobile_name"));
    }

    private UOMobile mapMobileData(ResultSet rs) throws SQLException {
        final var accountId = rs.getObject("account_id", UUID.class);
        if (accountId == null) {
            return new UONpc(MobileMapper.map(rs));
        }
        return new UOPlayer(MobileMapper.map(rs), accountId);
    }

    @Override
    public CompletableFuture<UOMobile> saveMobileFull(UOMobile mobile) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(INSERT_MOBILE_FULL)) {
                ps.setObject(1, mobile.getId());
                ps.setObject(2, mobile instanceof UOPlayer ? ((UOPlayer) mobile).getAccountId() : null);
                ps.setString(3, mobile.getName());
                ps.setInt(4, mobile.getModelId());
                ps.setInt(5, mobile.getHue());
                ps.setInt(6, mobile.getRace().getCode());
                ps.setInt(7, mobile.getGender().getCode());
                ps.setInt(8, mobile.getNotoriety().getCode());
                ps.setInt(9, mobile.getStatus().getCode());
                ps.setInt(10, mobile.getX());
                ps.setInt(11, mobile.getY());
                ps.setInt(12, mobile.getZ());
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new SQLException("Insert mobile returned no serial_id");
                    }

                    mobile.setSerialId(rs.getInt("serial_id"));
                }
                log.info("Mobile [{}-{}] saved!", mobile.getSerialId(), mobile.getName());
                return mobile;
            } catch (SQLException e) {
                throw new RuntimeException("Failed to save mobile", e);
            }
        }, executor);
    }

    @Override
    public CompletableFuture<Collection<UOMobile>> saveMobiles(Collection<UOMobile> mobiles) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement ps = conn.prepareStatement(UPDATE_MOBILES)) {
                for (UOMobile mobile : mobiles) {
                    ps.setObject(1, mobile.getId());
                    ps.setInt(2, mobile.getSerialId());
                    ps.setObject(3, mobile instanceof UOPlayer ? ((UOPlayer) mobile).getAccountId() : null);
                    ps.setString(4, mobile.getName());
                    ps.setInt(5, mobile.getModelId());
                    ps.setInt(6, mobile.getHue());
                    ps.setInt(7, mobile.getRace().getCode());
                    ps.setInt(8, mobile.getGender().getCode());
                    ps.setInt(9, mobile.getNotoriety().getCode());
                    ps.setInt(10, mobile.getStatus().getCode());
                    ps.addBatch();
                }
                ps.executeBatch();
                return mobiles;
            } catch (SQLException e) {
                throw new RuntimeException("Failed to batch save mobile vitals", e);
            }

        }, executor);
    }

    @Override
    public CompletableFuture<Collection<UOMobile>> saveRuntime(Collection<UOMobile> mobiles) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement ps = conn.prepareStatement(UPDATE_MOBILE_RUNTIME)) {
                for (UOMobile mobile : mobiles) {
                    ps.setInt(1, mobile.getX());
                    ps.setInt(2, mobile.getY());
                    ps.setInt(3, mobile.getZ());
                    ps.setInt(4, mobile.getDirection().getCode());
                    ps.setBoolean(5, mobile.isRunning());
                    ps.setInt(6, mobile.getHitpoints());
                    ps.setInt(7, mobile.getStamina());
                    ps.setInt(8, mobile.getMana());
                    ps.setObject(9, mobile.getId());
                    ps.addBatch();
                }
                ps.executeBatch();
                return mobiles;
            } catch (SQLException e) {
                throw new RuntimeException("Failed to batch save mobile runtime", e);
            }

        }, executor);
    }

    @Override
    public CompletableFuture<Collection<UOMobile>> saveVitals(Collection<UOMobile> mobiles) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement ps = conn.prepareStatement(UPDATE_MOBILE_VITALS)) {
                for (UOMobile mobile : mobiles) {
                    ps.setInt(1, mobile.getMaxHitpoints());
                    ps.setInt(2, mobile.getMaxStamina());
                    ps.setInt(3, mobile.getMaxMana());
                    ps.setObject(4, mobile.getId());
                    ps.addBatch();
                }
                ps.executeBatch();
                return mobiles;
            } catch (SQLException e) {
                throw new RuntimeException("Failed to batch save mobile vitals", e);
            }

        }, executor);
    }

    @Override
    public CompletableFuture<Collection<UOMobile>> saveAttributes(Collection<UOMobile> mobiles) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement ps = conn.prepareStatement(UPDATE_MOBILE_ATTRIBUTES)) {
                for (UOMobile mobile : mobiles) {
                    ps.setInt(1, mobile.getStrength());
                    ps.setInt(2, mobile.getDexterity());
                    ps.setInt(3, mobile.getIntelligence());
                    ps.setInt(4, mobile.getStatCap());
                    ps.setInt(5, mobile.getFollowers());
                    ps.setInt(6, mobile.getMaxFollowers());
                    ps.setInt(7, mobile.getLuck());
                    ps.setInt(8, mobile.getTithingPoints());
                    ps.setObject(9, mobile.getId());
                    ps.addBatch();
                }
                ps.executeBatch();
                return mobiles;
            } catch (SQLException e) {
                throw new RuntimeException("Failed to batch save mobile vitals", e);
            }

        }, executor);
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
