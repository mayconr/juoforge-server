package com.github.mayconr.juoserver.network.packet;

import com.github.mayconr.juoserver.game.item.template.ItemTemplate;
import com.github.mayconr.juoserver.game.model.*;

import java.util.Objects;

public class CharacterAnimationFactory {
    private CharacterAnimationFactory() {
    }

    public static CharacterAnimation wrestling(
            UOMobile attacker,
            int animationFrame
    ) {
        return attack(
                attacker,
                WeaponStyle.WRESTLING,
                animationFrame
        );
    }

    public static CharacterAnimation weapon(
            UOMobile attacker,
            ItemTemplate.Weapon weapon,
            int animationFrame
    ) {
        Objects.requireNonNull(weapon, "Weapon cannot be null");
        Objects.requireNonNull(weapon.style(), "Weapon style cannot be null");

        return attack(
                attacker,
                weapon.style(),
                animationFrame
        );
    }

    public static CharacterAnimation spell(
            UOMobile attacker,
            int animationFrame
    ) {
        return create(
                attacker,
                AnimationType.CAST_DIRECTED,
                animationFrame
        );
    }

    private static CharacterAnimation attack(
            UOMobile attacker,
            WeaponStyle style,
            int animationFrame
    ) {
        AnimationType animation = style.animationFor(attacker.isMounted());

        return create(
                attacker,
                animation,
                animationFrame
        );
    }

    private static CharacterAnimation create(
            UOMobile mobile,
            AnimationType animation,
            int animationFrame
    ) {
        return new CharacterAnimation(
                mobile,
                AnimationRepeat.ONCE,
                animation,
                animationFrame,
                AnimationDirection.FORWARD
        );
    }
}
