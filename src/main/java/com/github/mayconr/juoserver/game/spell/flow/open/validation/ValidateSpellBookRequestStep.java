package com.github.mayconr.juoserver.game.spell.flow.open.validation;

import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.spell.flow.open.OpenSpellBookContext;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

public class ValidateSpellBookRequestStep extends AbstractFlowStep<OpenSpellBookContext> {
    public ValidateSpellBookRequestStep() {
        super("ValidateSpellBookRequest");
    }

    @Override
    public StepResult execute(OpenSpellBookContext context) {
        if (context.getPlayer() == null || context.getBook() == null || context.getType() == null) {
            return StepResult.failure("Player, book and spellbook type are required");
        }
        var book = context.getBook();
        if (!UOItem.isItem(book.getSerialId()) || book.getModelId() < 0 || book.getModelId() > 0xFFFF) {
            return StepResult.failure("Invalid spellbook serial or model ID");
        }
        return StepResult.success();
    }
}
