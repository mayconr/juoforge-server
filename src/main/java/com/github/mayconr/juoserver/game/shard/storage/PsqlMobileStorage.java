package com.github.mayconr.juoserver.game.shard.storage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import javax.sql.DataSource;

import com.github.mayconr.juoserver.game.core.model.*;
import com.github.mayconr.juoserver.game.storage.mobile.MobileStorage;

public class PsqlMobileStorage extends AbstractStorage implements MobileStorage {

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

    @Override
    public CompletableFuture<UOPlayer> createNewPlayer(PlayerDetails details) {
        return null;
    }

    private AccountLoginMobile mapLoginMobile(ResultSet resultSet) throws SQLException {
        return new AccountLoginMobile(
                resultSet.getInt("serial_id"), resultSet.getString("mobile_name"));
    }

    private UOMobile mapMobileData(ResultSet rs) throws SQLException {
        final var accountId = rs.getObject("account_id", UUID.class);
        if (accountId == null) {
            return new UONpc(mapMobile(rs));
        }
        return new UOPlayer(mapMobile(rs), accountId);
    }

    private UOMobile mapMobile(ResultSet rs) throws SQLException {
        // --- Core identity ---
        UUID id = rs.getObject("mobile_id", UUID.class);
        int serialId = rs.getInt("serial_id");
        int modelId = rs.getInt("model_id");
        String name = rs.getString("name");

        int x = rs.getInt("x");
        int y = rs.getInt("y");
        int z = rs.getInt("z");

        int dirCode = rs.getInt("direction");
        Direction direction = rs.wasNull() ? Direction.SOUTH : Direction.fromCode(dirCode);

        int hue = rs.getInt("hue");

        int statusCode = rs.getInt("status");
        CharacterStatus status =
                rs.wasNull() ? CharacterStatus.NORMAL : CharacterStatus.fromCode(statusCode);

        int notorietyCode = rs.getInt("notoriety");
        Notoriety notoriety = rs.wasNull() ? Notoriety.INNOCENT : Notoriety.fromCode(notorietyCode);

        boolean running = rs.getBoolean("running");

        int raceCode = rs.getInt("race");
        Race race = rs.wasNull() ? Race.HUMAN : Race.fromCode(raceCode);

        int genderCode = rs.getInt("gender");
        Gender gender = rs.wasNull() ? Gender.MALE : Gender.fromCode(genderCode);

        // --- Attributes ---
        int strength = rs.getInt("strength");
        int dexterity = rs.getInt("dexterity");
        int intelligence = rs.getInt("intelligence");

        int statCap = rs.getInt("stat_cap");

        int followers = rs.getInt("followers");
        int maxFollowers = rs.getInt("max_followers");

        int luck = rs.getInt("luck");
        int tithingPoints = rs.getInt("tithing_points");

        // --- Vitals (runtime snapshot) ---
        int hitpoints = rs.getInt("hitpoints");
        int stamina = rs.getInt("stamina");
        int mana = rs.getInt("mana");

        // --- Max vitals (DB) ---
        int maxHitpoints = rs.getInt("max_hitpoints");
        int maxStamina = rs.getInt("max_stamina");
        int maxMana = rs.getInt("max_mana");

        int gold = 0;
        int weight = 0;
        int maxWeight = strength * 3;

        int physicalResist = 0;
        int maxPhysicalResist = 70;
        int fireResist = 0;
        int maxFireResist = 70;
        int coldResist = 0;
        int maxColdResist = 70;
        int poisonResist = 0;
        int maxPoisonResist = 70;
        int energyResist = 0;
        int maxEnergyResist = 70;

        int damageMin = 1;
        int damageMax = 4;

        int defenseChanceIncrease = 0;
        int maxDefenseChanceIncrease = 45;
        int hitChanceIncrease = 0;
        int swingSpeedIncrease = 0;
        int weaponDamageIncrease = 0;
        int lowerReagentCost = 0;
        int spellDamageIncrease = 0;
        int hpRegen = 0;
        int staminaRegen = 0;
        int manaRegen = 0;
        int reflectPhysicalDamage = 0;
        int enhancePotions = 0;
        int fasterCastRecovery = 0;
        int fasterCasting = 0;
        int lowerManaCost = 0;

        return new UOMobile(
                id,
                serialId,
                modelId,
                x,
                y,
                z,
                name,
                direction,
                hue,
                status,
                notoriety,
                running,
                race,
                gender,
                hitpoints,
                maxHitpoints,
                strength,
                dexterity,
                intelligence,
                stamina,
                maxStamina,
                mana,
                maxMana,
                gold,
                weight,
                maxWeight,
                statCap,
                followers,
                maxFollowers,
                physicalResist,
                maxPhysicalResist,
                fireResist,
                maxFireResist,
                coldResist,
                maxColdResist,
                poisonResist,
                maxPoisonResist,
                energyResist,
                maxEnergyResist,
                luck,
                damageMin,
                damageMax,
                tithingPoints,
                defenseChanceIncrease,
                maxDefenseChanceIncrease,
                hitChanceIncrease,
                swingSpeedIncrease,
                weaponDamageIncrease,
                lowerReagentCost,
                spellDamageIncrease,
                hpRegen,
                staminaRegen,
                manaRegen,
                reflectPhysicalDamage,
                enhancePotions,
                fasterCastRecovery,
                fasterCasting,
                lowerManaCost);
    }
}
