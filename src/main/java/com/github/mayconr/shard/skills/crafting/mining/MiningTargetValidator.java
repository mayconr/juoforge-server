package com.github.mayconr.shard.skills.crafting.mining;

import com.github.mayconr.juoserver.game.interaction.target.TargetResult;
import com.github.mayconr.juoserver.game.model.Location;
import com.github.mayconr.juoserver.game.model.TargetType;
import com.github.mayconr.juoserver.game.world.World;

public class MiningTargetValidator {

    public ValidationResult validate(World world, Location initialLocation, TargetResult result) {
        if (!TargetType.STATICS.equals(result.type())) {
            return ValidationResult.invalid("You must target a mineable surface");
        }

        final var mapTile = world.getLandTile(result.location());
        if (!MineableLandTile.isMineable(mapTile.id())) {
            return ValidationResult.invalid("Location cannot be mined");
        }

        return ValidationResult.valid();
    }

}
