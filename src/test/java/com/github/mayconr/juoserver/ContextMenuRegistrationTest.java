package com.github.mayconr.juoserver;

import com.github.mayconr.juoserver.game.ui.UIModule;
import com.github.mayconr.juoserver.game.ui.contextmenu.ContextMenuEntry;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class ContextMenuRegistrationTest {
    @Test
    void factoriesReceiveRuntimeAtBootstrapAndPreserveRegistrationOrder() {
        var configuration = new DefaultWorldConfigurator();
        var runtime = mock(ServerRuntime.class);
        var ui = mock(UIModule.class);
        var first = new ContextMenuEntry(3060001, context -> true);
        var second = new ContextMenuEntry(3060002, context -> true);
        var creations = new AtomicInteger();
        configuration.addContextMenuEntry(actual -> {
            assertSame(runtime, actual);
            creations.incrementAndGet();
            return first;
        });
        configuration.addContextMenuEntry(actual -> {
            assertSame(runtime, actual);
            creations.incrementAndGet();
            return second;
        });
        assertEquals(0, creations.get());

        WorldBootstrap.registerContextMenuEntries(ui, runtime, configuration);

        assertEquals(2, creations.get());
        verify(ui).registerContextMenuEntries(List.of(first, second));
    }

    @Test
    void rejectsNullFactoriesAndResults() {
        var configuration = new DefaultWorldConfigurator();
        assertThrows(NullPointerException.class, () -> configuration.addContextMenuEntry(null));
        configuration.addContextMenuEntry(runtime -> null);
        var ui = mock(UIModule.class);

        assertThrows(NullPointerException.class, () -> WorldBootstrap.registerContextMenuEntries(
                ui, mock(ServerRuntime.class), configuration));
        verifyNoInteractions(ui);
    }
}
