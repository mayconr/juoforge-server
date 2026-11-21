package com.github.mayconr.juoserver;

/** Registers a module's configuration before the world is bootstrapped. */
@FunctionalInterface
public interface UOModule {
    /**
     * Registers contributions, requiring dependencies before contributions that use them.
     * Runtime components should remain in factories or initializers.
     * A registration failure aborts configuration; contributions are not rolled back.
     */
    void register(ModuleConfiguration cfg);
}
