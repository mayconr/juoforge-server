package com.github.mayconr.juoserver.game.region.command;

import com.github.mayconr.juoserver.game.event.EventRegistry;
import com.github.mayconr.juoserver.game.model.event.Prompt;
import com.github.mayconr.juoserver.game.world.WorldInternal;
import lombok.RequiredArgsConstructor;

import java.util.function.Predicate;

@RequiredArgsConstructor
public class RegionPrompt implements EventRegistry<Prompt> {

    private final WorldInternal world;

    @Override
    public Class<Prompt> getType() {
        return Prompt.class;
    }

    @Override
    public Predicate<Prompt> getPredicate() {
        return prompt ->  "region".equals(prompt.name());
    }

    @Override
    public void handle(Prompt event) {
        var region = world.resolveRegion(event.player());

    }
}
