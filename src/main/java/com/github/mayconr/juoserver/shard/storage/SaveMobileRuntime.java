package com.github.mayconr.juoserver.shard.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mayconr.juoserver.game.model.UOMobile;
import lombok.RequiredArgsConstructor;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@RequiredArgsConstructor
public class SaveMobileRuntime {

    private static final String UPDATE_MOBILE_RUNTIME = """          
           
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
               ?, ?, ?, ?, ?, ?, ?, ?, ?, ?::jsonb, NOW()
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
               updated_at = NOW();
           """;

    private final DataSource dataSource;
    private final Executor executor;
    private final ObjectMapper objectMapper;

    public CompletableFuture<Collection<UOMobile>> saveRuntime(Collection<UOMobile> mobiles) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement ps = conn.prepareStatement(UPDATE_MOBILE_RUNTIME)) {
                for (UOMobile mobile : mobiles) {
                    ps.setObject(1, mobile.getId());
                    ps.setInt(2, mobile.getX());
                    ps.setInt(3, mobile.getY());
                    ps.setInt(4, mobile.getZ());
                    ps.setInt(5, mobile.getDirection().getCode());
                    ps.setBoolean(6, mobile.isRunning());
                    ps.setInt(7, mobile.getHitpoints());
                    ps.setInt(8, mobile.getStamina());
                    ps.setInt(9, mobile.getMana());
                    ps.setString(10, objectMapper.writeValueAsString(mobile.getAttrMap()));
                    ps.addBatch();
                }
                ps.executeBatch();
                return mobiles;
            } catch (Exception e) {
                throw new RuntimeException("Failed to batch save mobile runtime", e);
            }

        }, executor);
    }

}
