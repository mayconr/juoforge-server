package com.github.mayconr.juoserver.game.player;

import com.github.mayconr.juoserver.game.model.AccountMobile;
import com.github.mayconr.juoserver.game.model.UOAccount;
import com.github.mayconr.juoserver.game.model.UOPlayer;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/** Player operations without session events or client packets. */
public interface WorldPlayer {
    List<UOPlayer> getOnlinePlayers();

    CompletableFuture<List<AccountMobile>> getPlayerMobiles(UOAccount account);

    CompletableFuture<Void> deletePlayerMobile(int serialId);
}
