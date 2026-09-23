package com.github.mayconr.juoserver.game.spell.flow.open.notification;

import com.github.mayconr.juoserver.game.model.event.SpellBookOpened;
import com.github.mayconr.juoserver.game.spell.flow.open.OpenSpellBookContext;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

public class NotifySpellBookOpenedStep extends AbstractFlowStep<OpenSpellBookContext> {
    private final EventBus eventBus;

    public NotifySpellBookOpenedStep(EventBus eventBus) {
        super("NotifySpellBookOpened");
        this.eventBus = eventBus;
    }

    @Override
    public StepResult execute(OpenSpellBookContext context) {
        var book = context.getBook();
        eventBus.publish(new SpellBookOpened(context.getPlayer(), book.getSerialId(),
                book.getModelId(), context.getType(), context.getSpellMask()));
        return StepResult.success();
    }
}
