package com.github.mayconr.juoserver.game.shard.storage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class AbstractStorage {

    protected final DataSource dataSource;

    protected <D> Optional<D> findOne(
            String sql, SqlParameterBinder binder, RowMapper<D> function) {
        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            binder.bind(ps);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(function.map(rs));
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Error to find account", e);
        }

        return Optional.empty();
    }

    protected <D> List<D> findMany(String sql, SqlParameterBinder binder, RowMapper<D> mapper) {
        List<D> result = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            binder.bind(ps);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapper.map(rs));
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Error executing query", e);
        }

        return result;
    }
}
