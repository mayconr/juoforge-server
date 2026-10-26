package com.github.mayconr.juoserver.game.vitals.flow.regen;

import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractSyncFlowContext;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@RequiredArgsConstructor
public final class RegenContext extends AbstractSyncFlowContext<Void> {
    public static final String HP_ACCUMULATOR = "vitals.hpAccumulator";
    public static final String STAMINA_ACCUMULATOR = "vitals.staminaAccumulator";
    public static final String MANA_ACCUMULATOR = "vitals.manaAccumulator";

    private final UOMobile mobile;
    private final double elapsedSeconds;
    @Setter private ResourceRegen hitpoints;
    @Setter private ResourceRegen stamina;
    @Setter private ResourceRegen mana;

    public boolean isChanged() {
        return hitpoints.changed() || stamina.changed() || mana.changed();
    }

    public record ResourceRegen(int previous, int next, double remainder) {
        public boolean changed() {
            return previous != next;
        }
    }
}
