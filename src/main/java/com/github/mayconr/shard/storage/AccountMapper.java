package com.github.mayconr.shard.storage;

import com.github.mayconr.juoserver.game.model.AccountMobile;
import com.github.mayconr.juoserver.game.model.UOAccount;

import java.util.List;
import java.util.UUID;

public interface AccountMapper {

    UOAccount findByUsername(String username);

    List<AccountMobile> findAccountMobiles(UUID accountId);
}
