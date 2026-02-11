package com.github.mayconr.juoserver.game.world.animation;

import com.github.mayconr.juoserver.game.event.EventBus;
import com.github.mayconr.juoserver.game.model.AnimationOptions;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.model.event.AnimationSent;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AnimationService {

    private final EventBus eventBus;

    public void sendAnimation(UOMobile mobile, AnimationOptions options) {
        eventBus.publish(new AnimationSent(mobile, options));
        //fanout.writeAndFlush(new CharacterAnimation(player, options.repeat(), options.type(), options.frame(), options.direction()));
    }

}
