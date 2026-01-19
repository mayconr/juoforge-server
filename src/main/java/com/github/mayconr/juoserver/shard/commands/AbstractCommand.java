package com.github.mayconr.juoserver.shard.commands;

import java.util.function.Predicate;

import com.github.mayconr.juoserver.common.event.EventRegistry;
import com.github.mayconr.juoserver.common.event.Prompt;

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
