package com.github.mayconr.juoserver;

/** Registers a module's configuration before the world is bootstrapped. */
@FunctionalInterface
public interface UOModule {
    /** Registers the module using the configuration supplied to {@link WorldConfigurator#addModule(UOModule)}. */
    void register(ModuleConfiguration cfg);
}
