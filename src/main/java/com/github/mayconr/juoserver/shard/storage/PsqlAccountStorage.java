package com.github.mayconr.juoserver.shard.storage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import javax.sql.DataSource;

import com.github.mayconr.juoserver.game.model.UOAccount;
import com.github.mayconr.juoserver.infrastructure.storage.account.AccountStorage;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PsqlAccountStorage extends AbstractStorage implements AccountStorage {

    private final Executor executor;

    public PsqlAccountStorage(DataSource dataSource, Executor executor) {
        super(dataSource);
        this.executor = executor;
    }

    @Override
    public CompletableFuture<Optional<UOAccount>> findByUsername(String username) {
        return CompletableFuture.supplyAsync(
                () -> {
                    log.info("Loading account for username={}", username);
                    String sql =
                            """
                SELECT id, username, password
                FROM accounts
                WHERE username = ?
            """;
                    return findOne(sql, ps -> ps.setString(1, username), this::map);
                },
                executor);
    }

    @Override
    public CompletableFuture<Optional<UOAccount>> findById(String accountId) {
        return CompletableFuture.supplyAsync(
                () -> {
                    log.info("Loading account for id={}", accountId);

                    String sql =
                            """
                    SELECT id, username, password
                    FROM accounts
                    WHERE id = ?
                """;

                    return findOne(sql, ps -> ps.setObject(1, accountId), this::map);
                },
                executor);
    }

    private UOAccount map(ResultSet rs) throws SQLException {
        return new UOAccount(
                rs.getObject("id", UUID.class), rs.getString("username"), rs.getString("password"));
    }
}
