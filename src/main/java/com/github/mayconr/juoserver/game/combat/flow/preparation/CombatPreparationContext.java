package com.github.mayconr.juoserver.game.combat.flow.preparation;

import com.github.mayconr.juoserver.game.combat.CombatSession;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractContext;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@RequiredArgsConstructor
@Data
public class CombatPreparationContext extends AbstractContext {
    private final UOMobile attacker;
    private final int targetSerial;
    private final CombatOrigin origin;

    private UOMobile targetMobile;
    private CombatSession session;

    public static CombatPreparationContext of(UOMobile attacker, int targetSerial, CombatOrigin origin) {
        return new CombatPreparationContext(attacker, targetSerial, origin);
    }

    public sealed interface CombatOrigin {

        static RequestOrigin ofRequest() {
            return new RequestOrigin();
        }

        static SpellOrigin ofSpell() {
            return new SpellOrigin();
        }
    }

    public record RequestOrigin() implements CombatOrigin {}

    public record SpellOrigin() implements CombatOrigin {}
}
