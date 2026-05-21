package com.github.mayconr.juoserver.game.damage.flow.damage.notification;

import com.github.mayconr.juoserver.game.damage.flow.damage.DamageContext;
import com.github.mayconr.juoserver.game.model.LethalDamageEvent;
import com.github.mayconr.juoserver.game.model.event.MobileDamagedEvent;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

public class NotifyDamageStep extends AbstractFlowStep<DamageContext> {

    private final EventBus eventBus;

    public NotifyDamageStep(EventBus eventBus) {
        super("notify_damage");
        this.eventBus = eventBus;
    }

    @Override
    public StepResult execute(DamageContext context) {
        final var source = context.getSource();
        final var target = context.getTarget();
        final var kind = context.getSourceKind();

        if (context.isLethal()) {
            eventBus.publish(new LethalDamageEvent(source, target, kind));
        } else {
            eventBus.publish(
                    new MobileDamagedEvent(
                            source,
                            target,
                            kind,
                            context.getComponents(),
                            context.getTotalDamage(),
                            context.getOldHp(),
                            context.getNewHp()
                    )
            );
        }
        return StepResult.success();
    }
}
