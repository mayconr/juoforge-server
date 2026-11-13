package com.github.mayconr.juoserver.game.item.flow.lift.validation;

import com.github.mayconr.juoserver.game.model.Location;
import com.github.mayconr.juoserver.game.model.UOMobile;

/** Outermost holder of an item, found by walking up the containers that enclose it. */
public sealed interface ItemRoot {

    /** The item, or the outermost container enclosing it, lies on the ground at this location. */
    record OnGround(Location location) implements ItemRoot {
    }

    /** The item, or the outermost container enclosing it, is equipped by this mobile. */
    record CarriedBy(UOMobile mobile) implements ItemRoot {
    }

    /** The item is not placed in the world, or one of its containers or owner no longer exists. */
    record Unreachable() implements ItemRoot {
    }
}
