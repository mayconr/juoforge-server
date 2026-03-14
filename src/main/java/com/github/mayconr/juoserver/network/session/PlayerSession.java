package com.github.mayconr.juoserver.network.session;

import com.github.mayconr.juoserver.game.model.AccountMobile;
import com.github.mayconr.juoserver.game.model.UOAccount;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.network.packet.CreateCharacter;
import com.github.mayconr.juoserver.network.packet.DeleteCharacter;
import com.github.mayconr.juoserver.network.packet.LoginReject;

import java.net.SocketAddress;
import java.util.concurrent.CompletableFuture;

/**
 * Represents a connected player session.
 *
 * A PlayerSession controls the full lifecycle of a client connection,
 * including authentication, character selection, world attachment,
 * and disconnection.
 *
 * Implementations are responsible for enforcing valid state transitions.
 */
public interface PlayerSession {

    /**
     * Returns the currently active player entity.
     *
     * @return the attached UOPlayer instance, or null if not yet in world
     */
    UOPlayer getPlayer();

    /**
     * Forces the session to disconnect.
     *
     * This should trigger cleanup, world detachment,
     * persistence operations, and network shutdown.
     */
    void disconnect();

    /**
     * Marks the session as connected at the network level.
     *
     * @param remoteAddress the remote client socket address
     */
    void connect(SocketAddress remoteAddress);

    /**
     * Sets the client version reported during handshake.
     *
     * @param version the client version string
     */
    void setClientVersion(String version);

    /**
     * Marks the session as authenticated.
     *
     * @param account the authenticated account
     */
    void authenticate(UOAccount account);

    /**
     * Selects a character from the account character list.
     *
     * @param index the index of the selected character
     * @return the selected AccountMobile descriptor
     */
    AccountMobile selectCharacter(int index);

    /**
     * Prepares the selected character to enter the world.
     *
     * This includes loading full character data,
     * initializing runtime state, and attaching to the world.
     *
     * @return a future that completes when the player is ready to activate
     */
    CompletableFuture<UOPlayer> enteringWorld();

    /**
     * Activates the session in the world.
     *
     * After activation, the player can perform in-game actions
     * and will start receiving world updates.
     */
    void activate();

    /**
     * Rejects the login attempt with a specific reason.
     *
     * @param reason the rejection reason to be sent to the client
     */
    void reject(LoginReject.Reason reason);

    /**
     * Creates a new character associated with the authenticated account.
     *
     * @param character the character creation request data
     * @return a future that completes with the created UOPlayer
     */
    CompletableFuture<UOPlayer> createCharacter(CreateCharacter character);

    void deleteCharacter(DeleteCharacter deleteCharacter);
}
