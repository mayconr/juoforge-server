package com.github.mayconr.shard.storage;

import lombok.RequiredArgsConstructor;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@RequiredArgsConstructor
public class GetSerial {

    private final DataSource dataSource;
    private final Executor executor;

    public CompletableFuture<Integer> getNextSerial(String entity) {
        return CompletableFuture.supplyAsync(()->{
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement ps = conn.prepareStatement("SELECT next_serial FROM serial_counters WHERE entity_type = ? LIMIT 1")) {
                ps.setString(1, entity);

                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new SQLException("Serial counter not found for MOBILE");
                    }
                    return rs.getInt("next_serial");
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

}
