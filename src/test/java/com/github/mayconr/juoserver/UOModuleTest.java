package com.github.mayconr.juoserver;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import static org.mockito.Mockito.mock;

import static org.junit.jupiter.api.Assertions.*;

class UOModuleTest {
    @Test
    void defersInitializersAndPreservesRegistrationOrder() {
        var cfg = new DefaultWorldConfigurator();
        var calls = new ArrayList<String>();
        var runtime = mock(ServerRuntime.class);
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
        assertEquals(List.of("first", "second"), calls);
        assertThrows(NullPointerException.class, () -> cfg.addInitializer(null));
    }

    @Test
    void moduleConfigurationDoesNotExposeShardSettingsOrStorage() {
        var restricted = Set.of("serverSettings", "gameplaySettings", "content",
                "mobileStorage", "itemStorage", "accountStorage", "propertyStorage", "settings", "ai");
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
        assertEquals(List.of("module.messages"), cfg.messageBundles());
    }

    @Test
    void rejectsNullModules() {
        assertThrows(NullPointerException.class, () -> new DefaultWorldConfigurator().addModule(null));
    }
    @Test
    void registersSharedDependenciesOnceBeforeDependents() {
        var cfg = new DefaultWorldConfigurator();
        var calls = new ArrayList<String>();
        var shared = new TestModule(actual -> calls.add("shared"));
        UOModule first = actual -> {
            actual.requireModule(shared);
            calls.add("first");
        };
        UOModule second = actual -> {
            actual.requireModule(shared);
            calls.add("second");
        };

        cfg.addModule(first);
        cfg.addModule(second);
        cfg.addModule(shared);
        cfg.addModule(first);

        assertEquals(List.of("shared", "first", "second"), calls);
    }

    @Test
    void sharesExplicitRegistrationsWithDependencies() {
        var cfg = new DefaultWorldConfigurator();
        var calls = new ArrayList<String>();
        var shared = new TestModule(actual -> calls.add("shared"));
        cfg.addModule(shared);
        cfg.addModule(actual -> actual.requireModule(shared));
        assertEquals(List.of("shared"), calls);
    }

    @Test
    void rejectsDifferentInstancesOfTheSameModuleClass() {
        var cfg = new DefaultWorldConfigurator();
        cfg.addModule(new TestModule(actual -> {}));
        var failure = assertThrows(IllegalArgumentException.class,
                () -> cfg.requireModule(new TestModule(actual -> {})));
        assertTrue(failure.getMessage().contains(TestModule.class.getName()));
    }

    @Test
    void rejectsDirectCycles() {
        var cfg = new DefaultWorldConfigurator();
        var module = new TestModule(actual -> {});
        module.registration = actual -> actual.requireModule(module);
        var failure = assertThrows(IllegalArgumentException.class, () -> cfg.addModule(module));
        assertTrue(failure.getMessage().contains("Module dependency cycle"));
    }

    @Test
    void reportsIndirectCyclePath() {
        var cfg = new DefaultWorldConfigurator();
        var first = new TestModule(actual -> {});
        UOModule second = actual -> actual.requireModule(first);
        first.registration = actual -> actual.requireModule(second);

        var failure = assertThrows(IllegalArgumentException.class, () -> cfg.addModule(first));
        assertEquals("Module dependency cycle: " + first.getClass().getName()
                + " -> " + second.getClass().getName() + " -> " + first.getClass().getName(),
                failure.getMessage());
    }

    @Test
    void preventsRetryAfterPartialRegistrationFailure() {
        var cfg = new DefaultWorldConfigurator();
        var failure = new IllegalStateException("Registration failed");
        var module = new TestModule(actual -> {
            actual.addMessageBundle("partial.messages");
            throw failure;
        });

        assertSame(failure, assertThrows(IllegalStateException.class, () -> cfg.addModule(module)));
        assertThrows(IllegalStateException.class, () -> cfg.addModule(module));
        assertThrows(IllegalStateException.class, () -> cfg.requireModule(actual -> {}));
    }

    private static final class TestModule implements UOModule {
        private Consumer<ModuleConfiguration> registration;

        private TestModule(Consumer<ModuleConfiguration> registration) {
            this.registration = registration;
        }

        @Override
        public void register(ModuleConfiguration cfg) {
            registration.accept(cfg);
        }
    }
}
