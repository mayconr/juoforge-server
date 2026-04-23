package com.github.mayconr.juoserver.game.damage.flow.damage;

import com.github.mayconr.juoserver.game.flow.DamageFlowDefinition.DamageContext;
import com.github.mayconr.juoserver.game.model.LethalDamageEvent;
import com.github.mayconr.juoserver.game.model.event.MobileDamagedEvent;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.FlowPhase;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

public class NotifyDamageStep extends AbstractFlowStep<DamageContext> {

    private final EventBus eventBus;

    public NotifyDamageStep(EventBus eventBus) {
        super("notify_damage", 400, FlowPhase.CORE);
        this.eventBus = eventBus;
    }

    @Override
    public StepResult execute(DamageContext context) {
        if (context.isLethal()) {
            eventBus.publish(
                    new LethalDamageEvent(
                            context.getSource(),
                            context.getTarget(),
                            context.getSourceKind())
            );
            return StepResult.success();
        }

        eventBus.publish(
                new MobileDamagedEvent(
                        context.getSource(),
                        context.getTarget(),
                        context.getSourceKind(),
                        context.getComponents(),
                        context.getTotalDamage(),
                        context.getOldHp(),
                        context.getNewHp()
                )
        );
        return StepResult.success();
    }
}
