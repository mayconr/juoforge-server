package com.github.mayconr.juoserver.network.packet;

import com.github.mayconr.juoserver.game.model.AnimationDirection;
import com.github.mayconr.juoserver.game.model.AnimationRepeat;
import com.github.mayconr.juoserver.game.model.AnimationType;
import com.github.mayconr.juoserver.game.model.UOMobile;

public class CharacterAnimationFactory {
    private CharacterAnimationFactory() {
    }

    public static CharacterAnimation wrestling(UOMobile attacker, int animationFrame) {
        return new CharacterAnimation(
                attacker,
                AnimationRepeat.ONCE,
                attacker.isMounted() ? AnimationType.SWING_SWORD_FROM_HORSE : AnimationType.ATTACK_STANCE_SHORT,
                animationFrame,
                AnimationDirection.FORWARD
        );
    }

    public static CharacterAnimation weapon(UOMobile attacker, int animationFrame) {
        return new CharacterAnimation(
                attacker,
                AnimationRepeat.ONCE,
                attacker.isMounted() ? AnimationType.SWING_SWORD_FROM_HORSE : AnimationType.ATTACK_STANCE_SHORT,
                animationFrame,
                AnimationDirection.FORWARD
        );
    }

    public static CharacterAnimation ranged(UOMobile attacker, int animationFrame) {
        return new CharacterAnimation(
                attacker,
                AnimationRepeat.ONCE,
                attacker.isMounted() ? AnimationType.NORMAL_BOW_SHOT_ON_HORSE : AnimationType.BOW_SHOT,
                animationFrame,
                AnimationDirection.FORWARD
        );
    }

    public static CharacterAnimation spell(UOMobile attacker, int animationFrame) {
        return new CharacterAnimation(
                attacker,
                AnimationRepeat.ONCE,
                attacker.isMounted() ? AnimationType.NORMAL_BOW_SHOT_ON_HORSE : AnimationType.MAGIC_BUTTER_CHUM,
                animationFrame,
                AnimationDirection.FORWARD
        );
    }
}
