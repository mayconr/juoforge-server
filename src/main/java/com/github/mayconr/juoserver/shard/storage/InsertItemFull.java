package com.github.mayconr.juoserver.shard.storage;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.UOMobile;
import lombok.RequiredArgsConstructor;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@RequiredArgsConstructor
public class InsertItemFull {

    private static final String SAVE_ITEM_FULL = """
            WITH inserted_item AS (
                INSERT INTO items (
                    id,
                    serial_id,
                    name,
                    display_name,
                    type,
                    model_id,
                    hue,
                    layer,
                    unit_weight,
                    amount
                )
                VALUES (
                    ?,        -- id (UUID)
                    ?,        -- serial_id 
                    ?,        -- name
                    ?,        -- display_name
                    ?,        -- type
                    ?,        -- model_id
                    ?,        -- hue
                    ?,        -- layer
                    ?,        -- unit_weight
                    ?        -- amount                    
                )
            )
            INSERT INTO item_state (
                item_id,
                owner_mobile_id,
                parent_item_id,
                x,
                y,
                z,
                attr
            )
            VALUES (
                ?, -- item_id (UUID) → o MESMO id do item
                ?, -- owner_mobile_id
                ?, -- parent_item_id
                ?, -- x
                ?, -- y
                ?, -- z 
                ?::jsonb  -- attr            
            );
            """;

    private final DataSource dataSource;
    private final Executor executor;
    private final ObjectMapper objectMapper;

    public CompletableFuture<UOItem> saveItemFull(UOItem item) {
        return CompletableFuture.supplyAsync(()->{
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement ps = conn.prepareStatement(SAVE_ITEM_FULL)) {

                ps.setObject(1, item.getId());
                ps.setInt(2, item.getSerialId());
                ps.setString(3, item.getName());
                ps.setString(4, item.getDisplayName());
                ps.setInt(5, item.getType().getCode());
                ps.setInt(6, item.getModelId());
                ps.setInt(7, item.getHue());
                ps.setShort(8, (short)item.getLayer().getCode()); // nullable
                ps.setInt(9, 0); // weight
                ps.setInt(10, item.getAmount());

                // item_state

                ps.setObject(11, item.getId());
                ps.setObject(12, Optional.ofNullable(item.getOwner()).map(UOMobile::getId).orElse(null)); // mobile owner id. When equipped
                ps.setObject(13, Optional.ofNullable(item.getContainer()).map(UOItem.class::cast).map(UOItem::getId).orElse(null)); // container parent id. When in container
                ps.setInt(14, item.getX());
                ps.setInt(15, item.getY());
                ps.setInt(16, item.getZ());
                ps.setString(17, objectMapper.writeValueAsString(item.getAttrMap()));

                ps.executeUpdate();
                return item;
            } catch (SQLException e) {
                throw new RuntimeException("Unable to save item", e);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Unable to serialize attributes", e);
            }
        }, executor);
    }
}
