package com.github.mayconr.juoserver.game.skill;

import com.github.mayconr.juoserver.game.model.AnimationGroup;
import com.github.mayconr.juoserver.game.model.AnimationOptions;
import com.github.mayconr.juoserver.game.model.UOMobile;

/** Animation sent at the start of every swing cycle, resolved against the actor's current mount state. */
public record SkillActionAnimation(AnimationGroup group, int frames) {

    public AnimationOptions optionsFor(UOMobile actor) {
        return AnimationOptions.simpleForward(group.animationFor(actor.isMounted()), frames);
    }
}
