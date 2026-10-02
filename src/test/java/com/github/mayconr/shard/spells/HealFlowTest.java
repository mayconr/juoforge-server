package com.github.mayconr.shard.spells;

import com.github.mayconr.juoserver.game.model.UONpc;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.spell.template.SpellTemplate;
import com.github.mayconr.juoserver.game.spell.trigger.SpellCastContext;
import com.github.mayconr.juoserver.game.spell.trigger.SpellCastRegistry;
import com.github.mayconr.juoserver.game.messaging.WorldMessage;
import com.github.mayconr.juoserver.game.world.context.DefaultFlowFacade;
import com.github.mayconr.juoserver.game.world.context.DefaultFlowRegistry;
import com.github.mayconr.shard.spells.heal.HealContext;
import com.github.mayconr.shard.spells.heal.HealFlowDefinition;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HealFlowTest {
    @Test
    void healTriggerExecutesRegisteredFlowAndSendsMessage() {
        var messages = mock(WorldMessage.class);
        var flows = new DefaultFlowRegistry();
        flows.register("Heal", HealFlowDefinition.build(messages), HealContext.class);
        var triggers = new SpellCastRegistry();
        triggers.register(new HealSpellTrigger(new DefaultFlowFacade(flows)));
        var player = mock(UOPlayer.class);
        assertFalse(triggers.dispatch(new SpellCastContext(player,
                new SpellTemplate("magery:clumsy", 1, "Clumsy", null))));
        verifyNoInteractions(messages);
        assertTrue(triggers.dispatch(new SpellCastContext(player,
                new SpellTemplate("magery:heal", 4, "Heal", null))));
        verify(messages).send(player, "Foi heal");
        verifyNoMoreInteractions(messages);
    }

    @Test
    void rejectsMissingCasterAndSkipsClientMessageForNpc() {
        var messages = mock(WorldMessage.class);
        var flow = HealFlowDefinition.build(messages);
        assertTrue(flow.execute(new HealContext(null)).flowFailed());
        assertTrue(flow.execute(new HealContext(mock(UONpc.class))).flowSucceeded());
        verifyNoInteractions(messages);
    }
}
