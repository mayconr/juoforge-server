package com.github.mayconr.shard.storage;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mayconr.juoserver.game.model.UOItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@RequiredArgsConstructor
@Slf4j
public class SaveItemStates {

    private static final String UPDATE_STATES = """
              INSERT INTO item_state (
                  item_id,
                  owner_mobile_id,
                  parent_item_id,
                  x,
                  y,
                  z,
                  attr,
                  updated_at
              )
              VALUES (
                  ?, ?, ?, ?, ?, ?, ?::jsonb, NOW()
              )
              ON CONFLICT (item_id) DO UPDATE
              SET
                  owner_mobile_id = EXCLUDED.owner_mobile_id,
                  parent_item_id  = EXCLUDED.parent_item_id,
                  x               = EXCLUDED.x,
                  y               = EXCLUDED.y,
                  z               = EXCLUDED.z,
                  attr            = EXCLUDED.attr,
                  updated_at      = NOW();
            """;
    private final DataSource dataSource;
    private final Executor executor;
    private final ObjectMapper objectMapper;

    public CompletableFuture<Collection<UOItem>> saveStates(Collection<UOItem> items) {
        return CompletableFuture.supplyAsync(()->{
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement ps = conn.prepareStatement(UPDATE_STATES)) {
                for (UOItem item : items) {
                    ps.setObject(1, item.getId());
                    ps.setObject(2, item.getOwner() != null ? item.getOwner().getId() : null);
                    ps.setObject(3, Optional.ofNullable(item.getContainer()).map(UOItem.class::cast).map(UOItem::getId).orElse(null));

                    if (item.getOwner() == null) {
                        ps.setInt(4, item.getX());
                        ps.setInt(5, item.getY());
                        ps.setInt(6, item.getZ());
                    } else {
                        ps.setNull(4, Types.INTEGER);
                        ps.setNull(5, Types.INTEGER);
                        ps.setNull(6, Types.INTEGER);
                    }
                    ps.setString(7, objectMapper.writeValueAsString(item.getAttrMap()));

                    ps.addBatch();
                }
                ps.executeBatch();
                return items;
            } catch (SQLException e) {
                log.error("Unable to save items", e);
                throw new RuntimeException("Failed to batch save player vitals", e);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Unable to serialize attr map", e);
            }
        }, executor);
    }

}
