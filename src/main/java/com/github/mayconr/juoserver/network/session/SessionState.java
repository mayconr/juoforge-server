package com.github.mayconr.juoserver.network.session;

public enum SessionState {

    /**
     * The client is connected at the network level,
     * but has not yet authenticated.
     */
    CONNECTED,

    /**
     * The account credentials were validated successfully.
     * The player must now select or create a character.
     */
    AUTHENTICATED,

    /**
     * A character has been selected and is being prepared.
     * The player is not yet attached to the game world.
     */
    CHARACTER_SELECTED,

    /**
     * The server is sending initial world data
     * (map, status, inventory, nearby entities).
     */
    ENTERING_WORLD,

    /**
     * The player is fully attached to the world,
     * receiving ticks and allowed to perform in-game actions.
     */
    ACTIVE,

    /**
     * The session is shutting down.
     * Cleanup, persistence, and world detachment are in progress.
     */
    DISCONNECTING,

    /**
     * The session is fully terminated.
     * No further processing should occur.
     */
    DISCONNECTED
}
