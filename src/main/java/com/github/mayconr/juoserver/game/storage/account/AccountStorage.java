package com.github.mayconr.juoserver.game.storage.account;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import com.github.mayconr.juoserver.game.core.model.UOAccount;

public interface AccountStorage {

    CompletableFuture<Optional<UOAccount>> findByUsername(String username);

    CompletableFuture<Optional<UOAccount>> findById(String accountId);
}
