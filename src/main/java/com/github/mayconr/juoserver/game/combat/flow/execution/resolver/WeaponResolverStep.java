package com.github.mayconr.juoserver.game.combat.flow.execution.resolver;

import com.github.mayconr.juoserver.game.combat.EquippedWeaponResolver;
import com.github.mayconr.juoserver.game.combat.flow.execution.CombatExecutionContext;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import com.github.mayconr.juoserver.infrastructure.storage.WorldStorage;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WeaponResolverStep extends AbstractFlowStep<CombatExecutionContext> {

    private final WorldStorage storage;

    public WeaponResolverStep(WorldStorage storage) {
        super("WeaponResolverStep");
        this.storage = storage;
    }

    @Override
    public StepResult execute(CombatExecutionContext context) {
        var equipped = EquippedWeaponResolver.resolve(context.getSession().getAttacker(), storage);
        if (equipped.style() == null) {
            var weapon = equipped.item();
            log.warn("Equipped item '{}' (serial {}) has no weapon style defined in its template",
                    weapon.getName(), weapon.getSerialId());
            return StepResult.stop("COMBAT_WEAPON_STYLE_MISSING", "Equipped weapon has no style defined");
        }

        context.setWeapon(equipped.item());
        context.setWeaponStyle(equipped.style());
        return StepResult.success();
    }
}
