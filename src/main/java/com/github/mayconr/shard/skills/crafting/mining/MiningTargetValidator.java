package com.github.mayconr.shard.skills.crafting.mining;

import com.github.mayconr.juoserver.game.model.TargetResult;
import com.github.mayconr.juoserver.game.model.Location;
import com.github.mayconr.juoserver.game.map.WorldMap;

public class MiningTargetValidator {

    public ValidationResult validate(WorldMap map, Location initialLocation, TargetResult result) {
        if (!result.isStatic()) {
            return ValidationResult.invalid("You must target a mineable surface");
        }

        final var mapTile = map.getLandTile(result.location());
        if (!MineableLandTile.isMineable(mapTile.id())) {
            return ValidationResult.invalid("Location cannot be mined");
        }

        return ValidationResult.valid();
    }

}
