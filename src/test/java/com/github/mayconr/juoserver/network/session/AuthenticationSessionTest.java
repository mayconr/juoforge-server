package com.github.mayconr.juoserver.network.session;

import com.github.mayconr.juoserver.game.model.UOAccount;
import com.github.mayconr.juoserver.game.world.WorldInternal;
import com.github.mayconr.juoserver.infrastructure.storage.WorldStorage;
import com.github.mayconr.juoserver.infrastructure.storage.account.Accounts;
import com.github.mayconr.juoserver.network.packet.LoginReject;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class AuthenticationSessionTest {

    @Test
    void looksUpTheAccountInTheWorldStorage() {
        var channel = new EmbeddedChannel();
        try {
            var world = mock(WorldInternal.class, RETURNS_DEEP_STUBS);
            var storage = mock(WorldStorage.class);
            var accounts = mock(Accounts.class);
            when(storage.accounts()).thenReturn(accounts);
            when(accounts.findByUsername("admin")).thenReturn(new CompletableFuture<UOAccount>());
            var session = new NettyPlayerSession(channel, null, null, null, world, storage);

            session.authenticate("admin", "password");

            verify(accounts).findByUsername("admin");
        } finally {
            channel.finishAndReleaseAll();
        }
    }

    @Test
    void wrongPasswordCannotLoadPrivilegedAccountCharacters() {
        var channel = new EmbeddedChannel();
        try {
            var world = mock(WorldInternal.class);
            var storage = mock(WorldStorage.class);
            var accounts = mock(Accounts.class);
            when(storage.accounts()).thenReturn(accounts);
            when(accounts.findByUsername("admin")).thenReturn(CompletableFuture.completedFuture(
                    new UOAccount(UUID.randomUUID(), "admin", "correct", 20)));
            var session = new NettyPlayerSession(channel, null, null, null, world, storage);

            session.authenticate("admin", "wrong");
            channel.runPendingTasks();

            assertInstanceOf(LoginReject.class, channel.readOutbound());
            verifyNoInteractions(world);
        } finally {
            channel.finishAndReleaseAll();
        }
    }
}
