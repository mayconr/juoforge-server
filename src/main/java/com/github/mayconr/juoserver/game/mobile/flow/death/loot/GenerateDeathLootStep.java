package com.github.mayconr.juoserver.game.mobile.flow.death.loot;

import com.github.mayconr.juoserver.game.loot.LootRequest;
import com.github.mayconr.juoserver.game.loot.WorldLoot;
import com.github.mayconr.juoserver.game.mobile.flow.death.DeathContext;
import com.github.mayconr.juoserver.game.model.UONpc;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class GenerateDeathLootStep extends AbstractFlowStep<DeathContext> {
    private final WorldLoot loot;

    public GenerateDeathLootStep(WorldLoot loot) {
        super("generate-death-loot");
        this.loot = loot;
    }

    @Override
    public StepResult execute(DeathContext context) {
        if (!(context.getVictim() instanceof UONpc npc)) {
            return StepResult.skip();
        }
        try {
            loot.generate(new LootRequest(npc, context.getKiller(), context.getCause(), context.getCorpse()));
        } catch (RuntimeException failure) {
            // Death has already been applied: notification and AI cleanup must still happen.
            log.error("Loot generation failed for NPC {}", npc.getSerialId(), failure);
        }
        return StepResult.success();
    }
}
