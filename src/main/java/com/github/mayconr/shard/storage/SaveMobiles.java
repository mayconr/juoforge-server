package com.github.mayconr.shard.storage;

import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.model.UONpc;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import lombok.RequiredArgsConstructor;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@RequiredArgsConstructor
public class SaveMobiles {

    private static final String INSERT_MOBILES = """
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
                            -- Player attributes
                            account_id,
                            -- NPC attributes
                            mount_item_name
                        )
                        VALUES (
                            ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?
                        )
                        ON CONFLICT (id) DO UPDATE
                        SET
                            name       = EXCLUDED.name,
                            display_name = EXCLUDED.display_name,
                            model_id  = EXCLUDED.model_id,
                            hue       = EXCLUDED.hue,
                            race      = EXCLUDED.race,
                            gender    = EXCLUDED.gender,
                            notoriety = EXCLUDED.notoriety,
                            status    = EXCLUDED.status;
            """;

    private static final String UPDATE_SERIAL = """
            UPDATE serial_counters
            SET
                next_serial = ?,
                updated_at  = NOW()
            WHERE entity_type = 'MOBILE';
            """;

    private static final String DELETE_MOBILES = """
            DELETE FROM mobiles
            WHERE id = ?;
            """;

    private final DataSource dataSource;
    private final Executor executor;

    public CompletableFuture<Collection<UOMobile>> saveMobiles(int serial, Collection<UOMobile> mobiles, Collection<UOMobile> dirties) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = dataSource.getConnection();) {
                conn.setAutoCommit(false);

                try {
                    try (PreparedStatement ps = conn.prepareStatement(INSERT_MOBILES)) {
                        for (UOMobile mobile : mobiles) {
                            ps.setObject(1, mobile.getId());
                            ps.setInt(2, mobile.getSerialId());
                            ps.setString(3, mobile.getName());
                            ps.setString(4, mobile.getDisplayName());
                            ps.setInt(5, mobile.getModelId());
                            ps.setInt(6, mobile.getHue());
                            ps.setInt(7, mobile.getRace().getCode());
                            ps.setInt(8, mobile.getGender().getCode());
                            ps.setInt(9, mobile.getNotoriety().getCode());
                            ps.setInt(10, mobile.getStatus().getCode());

                            if (mobile instanceof UOPlayer player) {
                                ps.setObject(11, player.getAccountId());
                            } else {
                                ps.setObject(11, null);
                            }
                            if (mobile instanceof UONpc npc) {
                                ps.setString(12, npc.getMountItemName());
                            } else {
                                ps.setNull(12, Types.VARCHAR);
                            }

                            ps.addBatch();
                        }
                        ps.executeBatch();
                    }

                    try (PreparedStatement ps = conn.prepareStatement(UPDATE_SERIAL)) {
                        ps.setInt(1, serial);
                        ps.executeUpdate();
                    }

                    if (!dirties.isEmpty()) {
                        try (PreparedStatement ps = conn.prepareStatement(DELETE_MOBILES)) {
                            for (UOMobile mobile : dirties) {
                                ps.setObject(1, mobile.getId());
                                ps.addBatch();
                            }
                            ps.executeBatch();
                        }
                    }

                    conn.commit();
                    return mobiles;
                } catch (SQLException e) {
                    conn.rollback();
                    throw new RuntimeException("Unable to save mobiles", e);
                }
            } catch (SQLException e) {
                throw new RuntimeException("Failed to batch save player vitals", e);
            }

        }, executor);
    }

}
