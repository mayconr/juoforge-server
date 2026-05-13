package com.github.mayconr.shard.storage;

import com.github.mayconr.juoserver.game.model.AccountMobile;
import com.github.mayconr.juoserver.game.model.UOAccount;
import com.github.mayconr.juoserver.infrastructure.storage.AccountStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@RequiredArgsConstructor
@Slf4j
public class PsqlAccountStorage implements AccountStorage {

    private final SqlSessionFactory sessionFactory;
    private final Executor executor;

    @Override
    public CompletableFuture<UOAccount> getByUsername(String username) {
        return CompletableFuture.supplyAsync(
                () -> {
                    try (var session = sessionFactory.openSession()) {
                        return session.getMapper(AccountMapper.class).findByUsername(username);
                    }
                }, executor);
    }

    @Override
    public CompletableFuture<List<AccountMobile>> findAccountMobiles(UUID accountId) {
        return CompletableFuture.supplyAsync(
                () -> {
                    try (var session = sessionFactory.openSession()) {
                        return session.getMapper(AccountMapper.class).findAccountMobiles(accountId);
                    }
                }, executor);
    }
}
