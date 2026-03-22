package com.github.mayconr.juoserver.game.model;

import java.util.function.Consumer;

public record ContainerItemTarget(Container container, Options options) implements ItemTarget {
    public static ContainerItemTarget of(Container container) {
        return new ContainerItemTarget(container, Options.DEFAULT);
    }

    public static ContainerItemTarget of(Container container, Consumer<Options.Builder> cfg) {
        var builder = Options.builder();
        cfg.accept(builder);
        return new ContainerItemTarget(container, builder.build());
    }

    public record Options(
            boolean tryStack
    ) {

        public static final Options DEFAULT =
                new Options(true);

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {

            private boolean tryStack = true;

            public Builder tryStack(boolean value) {
                this.tryStack = value;
                return this;
            }

            public Options build() {
                return new Options(tryStack);
            }
        }
    }
}
