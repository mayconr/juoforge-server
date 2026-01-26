package com.github.mayconr.juoserver.shard.storage;

import com.github.mayconr.juoserver.game.model.UOMobile;
import lombok.RequiredArgsConstructor;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@RequiredArgsConstructor
public class SaveMobileAttributes {

    private static final String UPDATE_MOBILE_ATTRIBUTES = """
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
                ?, ?, ?, ?, ?, ?, ?, ?, ?
            )
            ON CONFLICT (mobile_id) DO UPDATE
            SET
                strength        = EXCLUDED.strength,
                dexterity       = EXCLUDED.dexterity,
                intelligence    = EXCLUDED.intelligence,
                stat_cap        = EXCLUDED.stat_cap,
                followers       = EXCLUDED.followers,
                max_followers   = EXCLUDED.max_followers,
                luck            = EXCLUDED.luck,
                tithing_points  = EXCLUDED.tithing_points;
            """;

    private final DataSource dataSource;
    private final Executor executor;

    public CompletableFuture<Collection<UOMobile>> saveAttributes(Collection<UOMobile> mobiles) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement ps = conn.prepareStatement(UPDATE_MOBILE_ATTRIBUTES)) {
                for (UOMobile mobile : mobiles) {
                    ps.setObject(1, mobile.getId());
                    ps.setInt(2, mobile.getStrength());
                    ps.setInt(3, mobile.getDexterity());
                    ps.setInt(4, mobile.getIntelligence());
                    ps.setInt(5, mobile.getStatCap());
                    ps.setInt(6, mobile.getFollowers());
                    ps.setInt(7, mobile.getMaxFollowers());
                    ps.setInt(8, mobile.getLuck());
                    ps.setInt(9, mobile.getTithingPoints());
                    ps.addBatch();
                }
                ps.executeBatch();
                return mobiles;
            } catch (Exception e) {
                throw new RuntimeException("Failed to batch save mobile vitals", e);
            }

        }, executor);
    }

}
