package com.github.mayconr.juoserver.game.spell.flow.open.validation;

import com.github.mayconr.juoserver.game.model.ContainerLocation;
import com.github.mayconr.juoserver.game.model.EquippedLocation;
import com.github.mayconr.juoserver.game.model.GameMath;
import com.github.mayconr.juoserver.game.model.GroundLocation;
import com.github.mayconr.juoserver.game.spell.flow.open.OpenSpellBookContext;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;

import java.util.HashSet;

public class ValidateSpellBookAccessStep extends AbstractFlowStep<OpenSpellBookContext> {
    private final RealmStorage storage;

    public ValidateSpellBookAccessStep(RealmStorage storage) {
        super("ValidateSpellBookAccess");
        this.storage = storage;
    }

    @Override
    public StepResult execute(OpenSpellBookContext context) {
        var player = context.getPlayer();
        var item = context.getBook();
        var visited = new HashSet<Integer>();
        while (item != null && visited.add(item.getSerialId())) {
            if (item.getCurrentLocation() instanceof EquippedLocation location) {
                return location.ownerSerialId() == player.getSerialId()
                        ? StepResult.success() : StepResult.failure("Spellbook belongs to another mobile");
            }
            if (item.getCurrentLocation() instanceof GroundLocation) {
                return GameMath.isInRange(player, item, 2)
                        ? StepResult.success() : StepResult.failure("Spellbook is out of reach");
            }
            if (item.getCurrentLocation() instanceof ContainerLocation location) {
                item = storage.getItem(location.containerSerialId()).orElse(null);
            } else {
                return StepResult.failure("Spellbook is not placed in the world");
            }
        }
        return StepResult.failure("Spellbook container is missing or cyclic");
    }
}
