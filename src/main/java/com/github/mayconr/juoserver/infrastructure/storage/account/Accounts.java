package com.github.mayconr.juoserver.infrastructure.storage.account;

import com.github.mayconr.juoserver.game.model.AccountMobile;
import com.github.mayconr.juoserver.game.model.UOAccount;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/** Account lookups, always resolved by the database adapter. */
public interface Accounts {

    CompletableFuture<UOAccount> findByUsername(String username);

    /** Characters available to the account, as shown during character selection. */
    CompletableFuture<List<AccountMobile>> mobiles(UOAccount account);
}
