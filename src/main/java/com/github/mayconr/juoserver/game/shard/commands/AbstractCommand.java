package com.github.mayconr.juoserver.game.shard.commands;

import com.github.mayconr.juoserver.game.core.event.EventRegistry;
import com.github.mayconr.juoserver.game.core.event.Prompt;

import java.util.function.Predicate;

public abstract class AbstractCommand implements EventRegistry<Prompt> {

    private final String command;

    public AbstractCommand(String command) {
        this.command = command;
    }

    @Override
    public Class<Prompt> getType() {
        return Prompt.class;
    }

    @Override
    public Predicate<Prompt> getPredicate() {
        return prompt -> prompt.name().equalsIgnoreCase(command);
    }
}
