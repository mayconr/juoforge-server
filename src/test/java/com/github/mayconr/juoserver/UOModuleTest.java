package com.github.mayconr.juoserver;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UOModuleTest {
    @Test
    void defersInitializersAndPreservesRegistrationOrder() {
        var cfg = new DefaultWorldConfigurator();
        var calls = new java.util.ArrayList<String>();
        var runtime = org.mockito.Mockito.mock(ServerRuntime.class);
        cfg.addModule(actual -> actual.addInitializer(value -> {
            assertSame(runtime, value);
            calls.add("first");
        }));
        var snapshot = cfg.initializerList();
        cfg.addInitializer(value -> calls.add("second"));

        assertTrue(calls.isEmpty());
        assertEquals(1, snapshot.size());
        assertThrows(UnsupportedOperationException.class, () -> snapshot.clear());
        cfg.initializerList().forEach(initializer -> initializer.accept(runtime));
        assertEquals(java.util.List.of("first", "second"), calls);
        assertThrows(NullPointerException.class, () -> cfg.addInitializer(null));
    }

    @Test
    void moduleConfigurationDoesNotExposeShardSettingsOrStorage() {
        var restricted = java.util.Set.of("serverSettings", "gameplaySettings", "content",
                "mobileStorage", "itemStorage", "accountStorage", "documentStorage", "settings", "ai");
        for (var method : ModuleConfiguration.class.getMethods()) {
            assertFalse(restricted.contains(method.getName()), method.toString());
        }
    }

    @Test
    void registersImmediatelyUsingTheSameConfiguration() {
        var cfg = new DefaultWorldConfigurator();
        cfg.addModule(actual -> {
            assertSame(cfg, actual);
            actual.addMessageBundle("module.messages");
        });
        assertEquals(java.util.List.of("module.messages"), cfg.messageBundles());
    }

    @Test
    void rejectsNullModules() {
        assertThrows(NullPointerException.class, () -> new DefaultWorldConfigurator().addModule(null));
    }
}
