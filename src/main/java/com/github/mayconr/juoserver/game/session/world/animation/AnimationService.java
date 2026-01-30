package com.github.mayconr.juoserver.game.session.world.animation;

import com.github.mayconr.juoserver.game.model.AnimationOptions;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.session.SessionFanout;
import com.github.mayconr.juoserver.network.packet.CharacterAnimation;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AnimationService {

    private final SessionFanout fanout;

    public void sendAnimation(UOMobile mobile, AnimationOptions options) {
        fanout.writeAndFlush(new CharacterAnimation(mobile, options.repeat(), options.type(), options.frame(), options.direction()));
    }

}
