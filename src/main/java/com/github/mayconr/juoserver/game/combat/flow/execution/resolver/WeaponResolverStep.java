package com.github.mayconr.juoserver.game.combat.flow.execution.resolver;

import com.github.mayconr.juoserver.game.combat.flow.execution.CombatExecutionContext;
import com.github.mayconr.juoserver.game.model.Layer;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;

import java.util.Optional;

public class WeaponResolverStep extends AbstractFlowStep<CombatExecutionContext> {

    private final RealmStorage storage;

    public WeaponResolverStep(RealmStorage storage) {
        super("WeaponResolverStep");
        this.storage = storage;
    }

    @Override
    public StepResult execute(CombatExecutionContext context) {
        final var session = context.getSession();
        final var mobile = session.getAttacker();
        final var equippedItems = mobile.getEquippedItems();

        if (equippedItems == null
                || equippedItems.isEmpty()
                || (!equippedItems.containsKey(Layer.ONE_HANDED) && !equippedItems.containsKey(Layer.TWO_HANDED))) {
            return StepResult.failure("No equipped items found");
        }
        final var weaponSerial = Optional.ofNullable(equippedItems.get(Layer.ONE_HANDED))
                .orElseGet(()->equippedItems.get(Layer.TWO_HANDED));
        final var weapon = storage.getItem(weaponSerial)
                .orElseThrow(()->new IllegalArgumentException("Weapon not found"));

        context.setWeapon(weapon);

        return StepResult.success();
    }
}
