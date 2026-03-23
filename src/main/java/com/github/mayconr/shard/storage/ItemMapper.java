package com.github.mayconr.shard.storage;

import com.github.mayconr.juoserver.game.model.AttributeMap;
import com.github.mayconr.juoserver.game.model.Layer;
import com.github.mayconr.juoserver.game.model.UOContainer;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.shard.storage.types.AttributesTypeHandler;
import com.github.mayconr.shard.storage.types.ItemFlagTypeHandler;
import com.github.mayconr.shard.storage.types.LayerTypeHandler;
import com.github.mayconr.shard.storage.types.UUIDTypeHandler;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface ItemMapper {

    @Select("""
            SELECT next_serial
            FROM serial_counters
            WHERE entity_type = 'ITEM'
            LIMIT 1""")
    Integer findNextItemSerial();

    @Update("""
        UPDATE serial_counters
        SET
            next_serial = #{nextSerial},
            updated_at  = NOW()
        WHERE entity_type = 'ITEM'
    """)
    int updateItemSerial(long serial);

    @Select("SELECT * FROM v_item_full WHERE serial_id = #{serialId} LIMIT 1")
    @ConstructorArgs({
            @Arg(column = "serial_id", javaType = int.class),
            @Arg(column = "model_id", javaType = int.class),
            @Arg(column = "x", javaType = int.class),
            @Arg(column = "y", javaType = int.class),
            @Arg(column = "z", javaType = int.class),
            @Arg(column = "name", javaType = String.class),
            @Arg(column = "display_name", javaType = String.class),
            @Arg(column = "attr", javaType = AttributeMap.class, typeHandler = AttributesTypeHandler.class),
            @Arg(column = "item_id", javaType = UUID.class, typeHandler = UUIDTypeHandler.class),
            @Arg(column = "layer", javaType = Layer.class, typeHandler = LayerTypeHandler.class),
            @Arg(column = "amount", javaType = int.class),
            @Arg(column = "hue", javaType = int.class),
            @Arg(column = "flags", javaType = List.class, typeHandler = ItemFlagTypeHandler.class),
            @Arg(column = "unit_weight", javaType = int.class),
            @Arg(column = "corpse_id", javaType = int.class)
    })
    @TypeDiscriminator(
            column = "type",
            javaType = String.class,
            cases = {
                    @Case(value = "C", type = UOContainer.class),
                    @Case(value = "O", type = UOItem.class)
            }
    )
    @Results(id = "itemMapping")
    UOItem findItemBySerialId(int serialId);

    @Select("SELECT * FROM v_item_full WHERE owner_mobile_id = #{mobileId}")
    @ResultMap("itemMapping")
    List<UOItem> findAllEquippedItems(UUID mobileId);

    @Select("SELECT * FROM v_item_full WHERE owner_mobile_id IS NULL AND parent_item_id IS NULL")
    @ResultMap("itemMapping")
    List<UOItem> findAllGroundItems();

    @Select("SELECT * FROM v_item_full WHERE parent_item_id = #{itemId}")
    @ResultMap("itemMapping")
    List<UOItem> findAllContainerItems(UUID itemId);

    @Insert("""
    INSERT INTO items (
        id,
        serial_id,
        owner_mobile_id,
        parent_item_id,
        name,
        display_name,
        model_id,
        hue,
        layer,
        unit_weight,
        amount,
        flags,
        corpse_id
    )
    VALUES (
        #{id},
        #{serialId},
        #{owner, typeHandler=com.github.mayconr.shard.storage.MobileIdTypeHandler},
        #{container, typeHandler=com.github.mayconr.shard.storage.types.ContainerTypeHandler},
        #{name},
        #{displayName},
        #{modelId},
        #{hue},
        #{layer},
        #{unitWeight},
        #{amount},
        #{flags, typeHandler=com.github.mayconr.shard.storage.types.ItemFlagTypeHandler}::jsonb,
        #{corpseId}
    )
    ON CONFLICT (id) DO UPDATE
    SET
        serial_id        = EXCLUDED.serial_id,
        owner_mobile_id  = EXCLUDED.owner_mobile_id,
        parent_item_id   = EXCLUDED.parent_item_id,
        name             = EXCLUDED.name,
        display_name     = EXCLUDED.display_name,
        model_id         = EXCLUDED.model_id,
        hue              = EXCLUDED.hue,
        layer            = EXCLUDED.layer,
        unit_weight      = EXCLUDED.unit_weight,
        amount           = EXCLUDED.amount,
        flags            = EXCLUDED.flags,
        corpse_id        = EXCLUDED.corpse_id
    """)
    void upsert(UOItem item);

    @Insert("""
        INSERT INTO item_state (
            item_id,
            x,
            y,
            z,
            attr,
            updated_at
        )
        VALUES (
            #{id},
            #{x},
            #{y},
            #{z},
            #{persistentAttrMap, typeHandler=com.github.mayconr.shard.storage.types.AttributesTypeHandler}::jsonb,
            NOW()
        )
        ON CONFLICT (item_id) DO UPDATE
        SET
            x          = EXCLUDED.x,
            y          = EXCLUDED.y,
            z          = EXCLUDED.z,
            attr       = EXCLUDED.attr,
            updated_at = NOW()
    """)
    int upsertItemState(UOItem state);

    @Delete("""
        DELETE FROM items
        WHERE id = #{id}
    """)
    int deleteById(UUID id);
}
