package com.github.mayconr.juoserver.infrastructure.storage;

import com.github.mayconr.juoserver.game.model.AccountMobile;
import com.github.mayconr.juoserver.game.model.UOAccount;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface AccountStorage {

    CompletableFuture<UOAccount> getByUsername(String username);

    CompletableFuture<List<AccountMobile>> findAccountMobiles(UUID accountId);
}
