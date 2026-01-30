package com.github.mayconr.shard.storage;

import com.github.mayconr.juoserver.game.model.UOMobile;
import lombok.RequiredArgsConstructor;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@RequiredArgsConstructor
public class SaveMobileVitals {

    private static final String UPDATE_MOBILE_VITALS = """
            INSERT INTO mobile_vitals (
                mobile_id,
                max_hitpoints,
                max_stamina,
                max_mana
            )
            VALUES (
                ?, ?, ?, ?
            )
            ON CONFLICT (mobile_id) DO UPDATE
            SET
                max_hitpoints = EXCLUDED.max_hitpoints,
                max_stamina   = EXCLUDED.max_stamina,
                max_mana      = EXCLUDED.max_mana;
            """;

    private final DataSource dataSource;
    private final Executor executor;

    public CompletableFuture<Collection<UOMobile>> saveVitals(Collection<UOMobile> mobiles) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement ps = conn.prepareStatement(UPDATE_MOBILE_VITALS)) {
                for (UOMobile mobile : mobiles) {
                    ps.setObject(1, mobile.getId());
                    ps.setInt(2, mobile.getMaxHitpoints());
                    ps.setInt(3, mobile.getMaxStamina());
                    ps.setInt(4, mobile.getMaxMana());
                    ps.addBatch();
                }
                ps.executeBatch();
                return mobiles;
            } catch (SQLException e) {
                throw new RuntimeException("Failed to batch save player vitals", e);
            }

        }, executor);
    }

}
