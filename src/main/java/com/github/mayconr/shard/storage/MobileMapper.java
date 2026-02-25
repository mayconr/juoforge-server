package com.github.mayconr.shard.storage;

import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.shard.storage.types.*;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface MobileMapper {

    @Select("SELECT next_serial FROM serial_counters WHERE entity_type = 'MOBILE' LIMIT 1")
    Integer findNextMobileSerial();

    @Update("""
        UPDATE serial_counters
        SET
            next_serial = #{serial},
            updated_at  = NOW()
        WHERE entity_type = 'MOBILE'
    """)
    int updateMobileSerial(long serial);

    @Select("SELECT EXISTS (SELECT 1 FROM mobiles WHERE LOWER(name) = LOWER(#{name}))")
    boolean mobileExists(String name);

    @Select("SELECT * FROM v_mobile_full WHERE serial_id = #{serialId}")
    @ConstructorArgs({
            @Arg(column = "serial_id",   javaType = int.class),
            @Arg(column = "model_id", javaType = int.class),
            @Arg(column = "x", javaType = int.class),
            @Arg(column = "y", javaType = int.class),
            @Arg(column = "z", javaType = int.class),
            @Arg(column = "name", javaType = String.class),
            @Arg(column = "display_name", javaType = String.class),
            @Arg(column = "attr", javaType = Map.class, typeHandler = AttributesTypeHandler.class),
    })
    @Results(id = "MobileMapping", value = {
            @Result(property = "hue", column = "hue"),
            @Result(property = "id", column = "mobile_id", typeHandler = UUIDTypeHandler.class),
            @Result(property = "race", column = "race", typeHandler = RaceTypeHandler.class),
            @Result(property = "gender", column = "gender", typeHandler = GenderTypeHandler.class),
            @Result(property = "notoriety", column = "notoriety", typeHandler = NotorietyTypeHandler.class),
            @Result(property = "status", column = "status", typeHandler = CharacterStatusTypeHandler.class),
            @Result(property = "direction", column = "direction", typeHandler =  DirectionTypeHandler.class),
            @Result(property = "running", column = "running"),
            @Result(property = "hitpoints", column = "hitpoints"),
            @Result(property = "maxHitpoints", column = "max_hitpoints"),
            @Result(property = "stamina", column = "stamina"),
            @Result(property = "maxStamina", column = "max_stamina"),
            @Result(property = "mana", column = "mana"),
            @Result(property = "maxMana", column = "max_mana"),
            @Result(property = "followers", column = "followers"),
            @Result(property = "maxFollowers", column = "max_followers"),
            @Result(property = "statCap", column = "stat_cap"),
            @Result(property = "luck", column = "luck"),
            @Result(property = "tithingPoints", column = "tithing_points"),
            @Result(property = "strength", column = "strength"),
            @Result(property = "dexterity", column = "dexterity"),
            @Result(property = "intelligence", column = "intelligence"),
    })
    @TypeDiscriminator(
            column = "type",
            javaType = String.class,
            cases = {
                    @Case(value = "P", type = UOPlayer.class),
                    @Case(value = "N", type = UONpc.class)
            }
    )
    UOMobile findMobileBySerialId(int serialId);

    @Select("SELECT * FROM v_mobile_full WHERE mobile_id = #{id}")
    @ResultMap("MobileMapping")
    UOMobile findMobileById(UUID id);

    @Select("SELECT * FROM v_mobile_full WHERE account_id is null")
    @ResultMap("MobileMapping")
    List<UOMobile> findAllNpcs();

    @Select("SELECT * FROM v_account_mobiles_login WHERE account_id = #{accountId}")
    @ConstructorArgs({
            @Arg(column = "serial_id",   javaType = int.class),
            @Arg(column = "mobile_name", javaType = String.class)
    })
    List<AccountMobile> findAccountMobilesByAccountId(UUID accountId);

    @Select("SELECT * FROM mobile_skills WHERE mobile_id = #{mobileId}")
    @ConstructorArgs({
        @Arg(column = "skill_id",   javaType = int.class),
        @Arg(column = "skill_base",   javaType = double.class),
        @Arg(column = "skill_cap",   javaType = double.class),
        @Arg(column = "skill_lock",   javaType = SkillLock.class, typeHandler = SkillLockTypeHandler.class),
    })
    List<SkillValue> findSkillsByMobileId(UUID mobileId);

    @Insert("""
        INSERT INTO mobiles (
            id,
            serial_id,
            name,
            display_name,
            model_id,
            hue,
            race,
            gender,
            notoriety,
            status,
            account_id
        )
        VALUES (
            #{id, jdbcType=OTHER},
            #{serialId},
            #{name},
            #{displayName},
            #{modelId},
            #{hue},
            #{race},
            #{gender},
            #{notoriety},
            #{status},
            #{accountId, jdbcType=OTHER}
        )
        ON CONFLICT (id) DO UPDATE
        SET
            name         = EXCLUDED.name,
            display_name = EXCLUDED.display_name,
            model_id     = EXCLUDED.model_id,
            hue          = EXCLUDED.hue,
            race         = EXCLUDED.race,
            gender       = EXCLUDED.gender,
            notoriety    = EXCLUDED.notoriety,
            status       = EXCLUDED.status
    """)
    int upsertPlayer(UOPlayer player);

    @Insert("""
        INSERT INTO mobiles (
            id,
            serial_id,
            name,
            display_name,
            model_id,
            hue,
            race,
            gender,
            notoriety,
            status
        )
        VALUES (
            #{id, jdbcType=OTHER},
            #{serialId},
            #{name},
            #{displayName},
            #{modelId},
            #{hue},
            #{race},
            #{gender},
            #{notoriety},
            #{status}
        )
        ON CONFLICT (id) DO UPDATE
        SET
            name         = EXCLUDED.name,
            display_name = EXCLUDED.display_name,
            model_id     = EXCLUDED.model_id,
            hue          = EXCLUDED.hue,
            race         = EXCLUDED.race,
            gender       = EXCLUDED.gender,
            notoriety    = EXCLUDED.notoriety,
            status       = EXCLUDED.status
    """)
    int upsertNpc(UONpc npc);

    @Delete("""
        DELETE FROM mobiles
        WHERE id = #{id};
    """)
    int deleteById(UUID id);

    @Insert("""
        INSERT INTO mobile_runtime (
            mobile_id,
            x,
            y,
            z,
            direction,
            running,
            hitpoints,
            stamina,
            mana,
            attr,
            updated_at
        )
        VALUES (
            #{id, jdbcType=OTHER},
            #{x},
            #{y},
            #{z},
            #{direction},
            #{running},
            #{hitpoints},
            #{stamina},
            #{mana},
            #{persistentAttrMap, typeHandler=com.github.mayconr.shard.storage.types.AttributesTypeHandler}::jsonb,
            NOW()
        )
        ON CONFLICT (mobile_id) DO UPDATE
        SET
            x          = EXCLUDED.x,
            y          = EXCLUDED.y,
            z          = EXCLUDED.z,
            direction  = EXCLUDED.direction,
            running    = EXCLUDED.running,
            hitpoints  = EXCLUDED.hitpoints,
            stamina    = EXCLUDED.stamina,
            mana       = EXCLUDED.mana,
            attr       = EXCLUDED.attr,
            updated_at = NOW()
    """)
    int upsertMobileRuntime(UOMobile mobile);

    @Insert("""
        INSERT INTO mobile_vitals (
            mobile_id,
            max_hitpoints,
            max_stamina,
            max_mana
        )
        VALUES (
            #{id, jdbcType=OTHER},
            #{maxHitpoints},
            #{maxStamina},
            #{maxMana}
        )
        ON CONFLICT (mobile_id) DO UPDATE
        SET
            max_hitpoints = EXCLUDED.max_hitpoints,
            max_stamina   = EXCLUDED.max_stamina,
            max_mana      = EXCLUDED.max_mana
    """)
    int upsertMobileVitals(UOMobile mobile);

    @Insert("""
        INSERT INTO mobile_attributes (
            mobile_id,
            strength,
            dexterity,
            intelligence,
            stat_cap,
            followers,
            max_followers,
            luck,
            tithing_points
        )
        VALUES (
            #{id, jdbcType=OTHER},
            #{strength},
            #{dexterity},
            #{intelligence},
            #{statCap},
            #{followers},
            #{maxFollowers},
            #{luck},
            #{tithingPoints}
        )
        ON CONFLICT (mobile_id) DO UPDATE
        SET
            strength       = EXCLUDED.strength,
            dexterity      = EXCLUDED.dexterity,
            intelligence   = EXCLUDED.intelligence,
            stat_cap       = EXCLUDED.stat_cap,
            followers      = EXCLUDED.followers,
            max_followers  = EXCLUDED.max_followers,
            luck           = EXCLUDED.luck,
            tithing_points = EXCLUDED.tithing_points
    """)
    int upsertMobileAttributes(UOMobile mobile);

    @Insert("""
        INSERT INTO mobile_skills (
            mobile_id,
            skill_id,
            skill_base,
            skill_cap,
            skill_lock
        ) VALUES (
            #{mobile.id, jdbcType=OTHER},
            #{skill.skillId, jdbcType=SMALLINT},
            #{skill.base, jdbcType=DOUBLE},
            #{skill.cap, jdbcType=DOUBLE},
            #{skill.lock, jdbcType=SMALLINT}
        )
        ON CONFLICT (mobile_id, skill_id)
        DO UPDATE SET
            skill_base = EXCLUDED.skill_base,
            skill_cap  = EXCLUDED.skill_cap,
            skill_lock = EXCLUDED.skill_lock
        """)
    void upsertSkill(@Param("mobile") UOMobile mobile, @Param("skill") SkillValue skill);
}
