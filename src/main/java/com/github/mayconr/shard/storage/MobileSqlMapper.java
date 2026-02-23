package com.github.mayconr.shard.storage;

import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.model.UONpc;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.shard.storage.types.*;
import org.apache.ibatis.annotations.*;

import java.util.Map;

public interface MobileSqlMapper {

    @Select("SELECT next_serial FROM serial_counters WHERE entity_type = 'MOBILE' LIMIT 1")
    Integer getNextMobileSerial();

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
    @Results({
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
    UOMobile getBySerialId(int serialId);
}
