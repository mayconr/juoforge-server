package com.github.mayconr.shard.storage;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mayconr.juoserver.game.model.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

class MobileMapper {
    public static UOMobile mapMobile(ResultSet rs) throws SQLException {
        // --- Core identity ---
        UUID id = rs.getObject("mobile_id", UUID.class);
        int serialId = rs.getInt("serial_id");
        int modelId = rs.getInt("model_id");
        String name = rs.getString("name");
        String displayName = rs.getString("display_name");

        Map<String, Object> attr;
        try {
            attr = new ObjectMapper().readValue(rs.getString("attr"), new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            attr = Collections.emptyMap();
        }

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
                displayName,
                attr,
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
                reflectPhysicalDamage,
                enhancePotions,
                fasterCastRecovery,
                fasterCasting,
                lowerManaCost);
    }

    public static SkillValue mapMobileSkills(ResultSet rs) throws SQLException {
        final var id = rs.getObject("id", UUID.class);
        final var skillId = rs.getInt("skill_id");
        final var base = rs.getDouble("skill_base");
        final var cap = rs.getDouble("skill_cap");
        final var lock = SkillLock.fromCode(rs.getInt("skill_lock"));
        return SkillValue.of(id, skillId, base, cap, lock);
    }
}
