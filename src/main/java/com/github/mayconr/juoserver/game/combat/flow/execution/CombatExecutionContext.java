package com.github.mayconr.juoserver.game.combat.flow.execution;

import com.github.mayconr.juoserver.game.combat.CombatSession;
import com.github.mayconr.juoserver.game.model.DamageComponent;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.WeaponStyle;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractAsyncFlowContext;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@RequiredArgsConstructor
@Data
public class CombatExecutionContext extends AbstractAsyncFlowContext<Void> {
    private final CombatSession session;

    private int hitFrame;
    private int animFrame;

    private WeaponStyle style;
    private int attackSpeed;
    private List<DamageComponent> damages;

    private UOItem weapon;
    private int maxDistance;

}
