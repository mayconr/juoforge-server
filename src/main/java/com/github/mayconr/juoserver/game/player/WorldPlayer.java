package com.github.mayconr.juoserver.game.player;

import com.github.mayconr.juoserver.game.model.AccountMobile;
import com.github.mayconr.juoserver.game.model.UOAccount;
import com.github.mayconr.juoserver.game.model.UOPlayer;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/** Player operations without session events or client packets. */
public interface WorldPlayer {
    /** Returns an immutable snapshot of the currently online players. */
    List<UOPlayer> getOnlinePlayers();

    /** Asynchronously retrieves the characters associated with an account. */
    CompletableFuture<List<AccountMobile>> getPlayerMobiles(UOAccount account);

    /** Asynchronously deletes the character identified by its serial. */
    CompletableFuture<Void> deletePlayerMobile(int serialId);
}
