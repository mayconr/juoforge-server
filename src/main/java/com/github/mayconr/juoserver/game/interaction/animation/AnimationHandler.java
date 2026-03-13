package com.github.mayconr.juoserver.game.interaction.animation;

import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.game.model.AnimationOptions;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.model.event.AnimationSent;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AnimationHandler {

    private final EventBus eventBus;

    public void sendAnimation(UOMobile mobile, AnimationOptions options) {
        eventBus.publish(new AnimationSent(mobile, options));
        //fanout.writeAndFlush(new CharacterAnimation(player, options.repeat(), options.type(), options.frame(), options.direction()));
    }

}
