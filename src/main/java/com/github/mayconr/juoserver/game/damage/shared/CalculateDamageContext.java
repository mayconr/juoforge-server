package com.github.mayconr.juoserver.game.damage.shared;

import com.github.mayconr.juoserver.game.combat.flow.execution.CombatType;
import com.github.mayconr.juoserver.game.item.template.ItemTemplate;
import com.github.mayconr.juoserver.game.model.DamageComponent;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.UOMobile;

import java.util.List;

public interface CalculateDamageContext {

    void addDamage(DamageComponent damage);

    List<DamageComponent> damages();

    DamageRequest damageRequest();

    record DamageRequest(
            CombatType combatType,
            UOItem weapon,
            UOMobile attacker,
            UOMobile target
    ) { }
}
