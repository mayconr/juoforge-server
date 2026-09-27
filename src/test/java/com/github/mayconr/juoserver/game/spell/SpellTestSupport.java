package com.github.mayconr.juoserver.game.spell;

import com.github.mayconr.juoserver.game.spell.flow.cast.CastSpellContext;
import com.github.mayconr.juoserver.game.spell.flow.cast.CastSpellFlowDefinition;
import com.github.mayconr.juoserver.game.spell.template.SpellTemplate;
import com.github.mayconr.juoserver.game.spell.trigger.SpellCastRegistry;
import com.github.mayconr.juoserver.game.world.context.DefaultFlowFacade;
import com.github.mayconr.juoserver.game.world.context.DefaultFlowRegistry;
import com.github.mayconr.juoserver.game.world.context.DefaultModuleContext;
import com.github.mayconr.juoserver.infrastructure.template.TemplateRegistry;

final class SpellTestSupport {
    static SpellModuleImpl module(TemplateRegistry<String, SpellTemplate> spells, SpellCastRegistry triggers) {
        var module = new SpellModuleImpl(spells);
        var registry = new DefaultFlowRegistry();
        registry.register("CastSpell", CastSpellFlowDefinition.build(spells, triggers), CastSpellContext.class);
        module.initialize(DefaultModuleContext.builder().flowFacade(new DefaultFlowFacade(registry)).build());
        return module;
    }
}
