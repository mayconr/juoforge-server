package com.github.mayconr.shard.spells;

import com.github.mayconr.juoserver.game.item.trigger.ItemUseContext;
import com.github.mayconr.juoserver.game.item.trigger.ItemUseTrigger;
import com.github.mayconr.juoserver.game.item.trigger.Trigger;
import com.github.mayconr.juoserver.game.model.SpellbookType;
import com.github.mayconr.juoserver.game.spell.WorldSpell;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SpellbookUseTrigger implements ItemUseTrigger {

    private final WorldSpell spells;

    @Override
    public boolean supports(ItemUseContext ctx) {
        return ctx.trigger() == Trigger.DOUBLE_CLICK
                && "spellbook".equals(ctx.item().getName());
    }

    @Override
    public void execute(ItemUseContext ctx) {
        // The shard currently provides a full Magery spellbook.
        spells.openSpellBook(ctx.player(), ctx.item(), SpellbookType.MAGERY, -1L);
    }
}
