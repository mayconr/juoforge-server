package com.github.mayconr.shard.spells;

import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.spell.trigger.SpellCastContext;
import com.github.mayconr.juoserver.game.spell.trigger.SpellCastTrigger;
import com.github.mayconr.juoserver.game.world.World;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class HealSpellTrigger implements SpellCastTrigger {

    private static final String SPELL_ID = "magery:heal";
    private final World world;

    @Override
    public boolean supports(SpellCastContext context) {
        return SPELL_ID.equals(context.spell().key());
    }

    @Override
    public void execute(SpellCastContext context) {
        world.sendMessage((UOPlayer) context.caster(), "Foi heal");
    }
}
