package com.github.mayconr.juoserver.game.damage.flow.damage;

import com.github.mayconr.juoserver.game.model.DamageComponent;
import com.github.mayconr.juoserver.game.model.DamageSourceKind;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractSyncFlowContext;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public final class DamageContext extends AbstractSyncFlowContext<Void> {
    private final UOMobile source;
    private final UOMobile target;
    private final DamageSourceKind sourceKind;
    private final List<DamageComponent> components;

    private int totalDamage;
    private int oldHp;
    private int newHp;
    private boolean lethal;

    public static DamageContext of(UOMobile source, UOMobile target, DamageSourceKind kind, DamageComponent... components) {
        return new DamageContext(source, target, kind, List.of(components));
    }

    public static DamageContext of(UOMobile source, UOMobile target, DamageSourceKind kind, List<DamageComponent> components) {
        return new DamageContext(source, target, kind, List.copyOf(components));
    }

    private DamageContext(UOMobile source, UOMobile target, DamageSourceKind sourceKind, List<DamageComponent> components) {
        this.source = source;
        this.target = target;
        this.sourceKind = sourceKind;
        this.components = components;
    }
}
