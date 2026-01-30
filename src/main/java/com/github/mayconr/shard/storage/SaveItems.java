package com.github.mayconr.shard.storage;

import com.github.mayconr.juoserver.game.model.UOItem;
import lombok.RequiredArgsConstructor;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@RequiredArgsConstructor
public class SaveItems {

    private static final String SAVE_ITEMS = """
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
                ?, ?, ?, ?, ?, ?, ?, ?, ?, ?
            )
            ON CONFLICT (id) DO UPDATE
            SET
                serial_id    = EXCLUDED.serial_id,
                name         = EXCLUDED.name,
                display_name = EXCLUDED.display_name,
                type         = EXCLUDED.type,
                model_id     = EXCLUDED.model_id,
                hue          = EXCLUDED.hue,
                layer        = EXCLUDED.layer,
                unit_weight  = EXCLUDED.unit_weight,
                amount       = EXCLUDED.amount;
            """;

    private static final String UPDATE_SERIAL = """
            UPDATE serial_counters
            SET
                next_serial = ?,
                updated_at  = NOW()
            WHERE entity_type = 'ITEM';
            """;

    private static final String DELETE_ITEMS = """
            DELETE FROM items
            WHERE id = ?;
            """;

    private final DataSource dataSource;
    private final Executor executor;

    public CompletableFuture<Collection<UOItem>> save(int serial, Collection<UOItem> items, Collection<UOItem> dirties) {
        return CompletableFuture.supplyAsync(()->{
            try (Connection conn = dataSource.getConnection();) {
                conn.setAutoCommit(false);
                try {
                    try (PreparedStatement ps = conn.prepareStatement(SAVE_ITEMS)) {
                        for (UOItem item : items) {
                            ps.setObject(1, item.getId());
                            ps.setInt(2, item.getSerialId());
                            ps.setString(3, item.getName());
                            ps.setString(4, item.getDisplayName());
                            ps.setInt(5, item.getType().getCode());
                            ps.setInt(6, item.getModelId());
                            ps.setInt(7, item.getHue());
                            ps.setInt(8, item.getLayer().getCode());
                            ps.setInt(9, 0);
                            ps.setInt(10, item.getAmount());
                            ps.addBatch();
                        }
                        ps.executeBatch();
                    }

                    try (PreparedStatement ps = conn.prepareStatement(UPDATE_SERIAL)) {
                        ps.setInt(1, serial);
                        ps.executeUpdate();
                    }

                    if (dirties!= null && !dirties.isEmpty()) {
                        try (PreparedStatement ps = conn.prepareStatement(DELETE_ITEMS)) {
                            for (UOItem item : dirties) {
                                ps.setObject(1, item.getId());
                                ps.addBatch();
                            }
                            ps.executeBatch();
                        }
                    }

                    conn.commit();
                    return items;
                } catch (SQLException e) {
                    conn.rollback();
                    throw new RuntimeException("Unable to save items", e);
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to batch save player vitals", e);
            }
        }, executor);
    }

}
