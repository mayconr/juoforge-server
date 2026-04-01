package com.github.mayconr.shard.storage;

import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.UOItemData;
import com.github.mayconr.shard.storage.types.AttributesTypeHandler;
import com.github.mayconr.shard.storage.types.ItemFlagTypeHandler;
import com.github.mayconr.shard.storage.types.LayerTypeHandler;
import com.github.mayconr.shard.storage.types.UUIDTypeHandler;
import org.apache.ibatis.annotations.*;

import java.util.List;
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

    @Select("""
        SELECT * 
        FROM v_item_full 
        WHERE serial_id = #{serialId} 
        LIMIT 1
    """)
    @Results(id = "itemDataMapping", value = {
            @Result(property = "serialId", column = "serial_id"),
            @Result(property = "modelId", column = "model_id"),
            @Result(property = "x", column = "x"),
            @Result(property = "y", column = "y"),
            @Result(property = "z", column = "z"),
            @Result(property = "name", column = "name"),
            @Result(property = "displayName", column = "display_name"),
            @Result(property = "persistentAttrMap",
                    column = "attr",
                    typeHandler = AttributesTypeHandler.class),
            @Result(property = "id",
                    column = "item_id",
                    typeHandler = UUIDTypeHandler.class),
            @Result(property = "layer",
                    column = "layer",
                    typeHandler = LayerTypeHandler.class),
            @Result(property = "amount", column = "amount"),
            @Result(property = "hue", column = "hue"),
            @Result(property = "flags",
                    column = "flags",
                    typeHandler = ItemFlagTypeHandler.class),
            @Result(property = "unitWeight", column = "unit_weight"),
            @Result(property = "ownerSerialId", column = "owner_serial_id"),
            @Result(property = "containerSerialId", column = "container_serial_id"),
            @Result(property = "containerGumpId", column = "container_gump_id"),
            @Result(property = "corpseId", column = "corpse_id")
    })
    UOItemData findItemBySerialId(int serialId);

    @Select("SELECT * FROM v_item_full WHERE owner_serial_id = #{ownerSerialId}")
    @ResultMap("itemMapping")
    List<UOItem> findAllEquippedItems(int ownerSerialId);

    @Select("SELECT * FROM v_item_full WHERE owner_serial_id IS NULL AND container_serial_id IS NULL")
    @ResultMap("itemMapping")
    List<UOItem> findAllGroundItems();

    @Select("SELECT * FROM v_item_full WHERE container_serial_id = #{containerSerialId}")
    @ResultMap("itemMapping")
    List<UOItem> findAllContainerItems(int containerSerialId);

    @Insert("""
    INSERT INTO items (
        id,
        serial_id,
        owner_serial_id,
        container_serial_id,
        name,
        display_name,
        model_id,
        hue,
        layer,
        unit_weight,
        amount,
        flags,
        container_gump_id,
        corpse_id
    )
    VALUES (
        #{id},
        #{serialId},
        #{ownerSerialId},
        #{containerSerialId},
        #{name},
        #{displayName},
        #{modelId},
        #{hue},
        #{layer},
        #{unitWeight},
        #{amount},
        #{flags, typeHandler=com.github.mayconr.shard.storage.types.ItemFlagTypeHandler}::jsonb,
        #{containerGumpId},
        #{corpseId}
    )
    ON CONFLICT (id) DO UPDATE
    SET
        serial_id        = EXCLUDED.serial_id,
        owner_serial_id  = EXCLUDED.owner_serial_id,
        container_serial_id   = EXCLUDED.container_serial_id,
        name             = EXCLUDED.name,
        display_name     = EXCLUDED.display_name,
        model_id         = EXCLUDED.model_id,
        hue              = EXCLUDED.hue,
        layer            = EXCLUDED.layer,
        unit_weight      = EXCLUDED.unit_weight,
        amount           = EXCLUDED.amount,
        flags            = EXCLUDED.flags,
        container_gump_id = EXCLUDED.container_gump_id,
        corpse_id        = EXCLUDED.corpse_id
    """)
    void upsert(UOItemData data);

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
    int upsertItemState(UOItemData data);

    @Delete("""
        DELETE FROM items
        WHERE id = #{id}
    """)
    int deleteById(UUID id);
}
