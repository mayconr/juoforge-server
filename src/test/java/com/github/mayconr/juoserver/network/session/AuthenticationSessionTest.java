package com.github.mayconr.juoserver.network.session;

import com.github.mayconr.juoserver.game.model.UOAccount;
import com.github.mayconr.juoserver.game.world.WorldInternal;
import com.github.mayconr.juoserver.infrastructure.storage.WorldStorage;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthenticationSessionTest {

    @Test
    void looksUpTheAccountInTheWorldStorage() {
        var channel = new EmbeddedChannel();
        try {
            var world = mock(WorldInternal.class, RETURNS_DEEP_STUBS);
            var storage = mock(WorldStorage.class);
            when(storage.getAccountByUsername("admin")).thenReturn(new CompletableFuture<UOAccount>());
            var session = new NettyPlayerSession(channel, null, null, null, world, storage);

            session.authenticate("admin");

            verify(storage).getAccountByUsername("admin");
        } finally {
            channel.finishAndReleaseAll();
        }
    }
}
