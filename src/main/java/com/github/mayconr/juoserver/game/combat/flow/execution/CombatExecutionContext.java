package com.github.mayconr.juoserver.game.combat.flow.execution;

import com.github.mayconr.juoserver.game.combat.CombatSession;
import com.github.mayconr.juoserver.game.damage.shared.CalculateDamageContext;
import com.github.mayconr.juoserver.game.model.DamageComponent;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.WeaponStyle;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractAsyncFlowContext;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@RequiredArgsConstructor
@Data
@ToString
public class CombatExecutionContext extends AbstractAsyncFlowContext<Void>
        implements CalculateDamageContext {
    private final CombatSession session;

    private int hitFrame;
    private int animFrame;

    private boolean mounted;

    private CombatType combatType;
    private int combatRadius;
    private int attackSpeed;
    private final List<DamageComponent> damages = new ArrayList<>();

    private UOItem weapon;
    private WeaponStyle weaponStyle;

    @Override
    public void addDamage(DamageComponent damage) {
        damages.add(damage);
    }

    @Override
    public List<DamageComponent> damages() {
        return damages;
    }

    @Override
    public DamageRequest damageRequest() {
        return new DamageRequest(combatType, weapon, session.getAttacker(), session.getTarget());
    }
}
