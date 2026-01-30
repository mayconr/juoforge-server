package com.github.mayconr.shard.storage;

import com.github.mayconr.juoserver.game.model.UOAccount;
import com.github.mayconr.juoserver.infrastructure.storage.AccountStorage;
import com.github.mayconr.juoserver.infrastructure.storage.DataNotFoundException;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Slf4j
public class PsqlAccountStorage extends AbstractStorage implements AccountStorage {

    private static final String SELECT_ACCOUNT = """
            SELECT id, username, password
            FROM accounts
            WHERE username = ?
            """;

    private final Executor executor;

    public PsqlAccountStorage(DataSource dataSource, Executor executor) {
        super(dataSource);
        this.executor = executor;
    }

    @Override
    public CompletableFuture<UOAccount> findByUsername(String username) {
        return CompletableFuture.supplyAsync(
                () -> {
                    log.info("Loading account for username={}", username);
                    return findOne(SELECT_ACCOUNT, ps -> ps.setString(1, username), this::map)
                            .orElseThrow(()->new DataNotFoundException("Account not found for username "+username));
                },
                executor);
    }

    private UOAccount map(ResultSet rs) throws SQLException {
        return new UOAccount(
                rs.getObject("id", UUID.class), rs.getString("username"), rs.getString("password"));
    }
}
