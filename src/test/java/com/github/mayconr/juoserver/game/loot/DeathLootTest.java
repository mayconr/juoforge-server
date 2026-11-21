package com.github.mayconr.juoserver.game.loot;

import com.github.mayconr.juoserver.game.mobile.flow.death.DeathContext;
import com.github.mayconr.juoserver.game.mobile.flow.death.DeathFlowDefinition;
import com.github.mayconr.juoserver.game.mobile.flow.death.loot.GenerateDeathLootStep;
import com.github.mayconr.juoserver.game.item.ItemCreationResult;
import com.github.mayconr.juoserver.game.item.ItemModule;
import com.github.mayconr.juoserver.game.item.template.ItemTemplate;
import com.github.mayconr.juoserver.game.model.DeathCause;
import com.github.mayconr.juoserver.game.model.ItemLocationType;
import com.github.mayconr.juoserver.game.model.UOCorpse;
import com.github.mayconr.juoserver.game.model.UOItemData;
import com.github.mayconr.juoserver.game.model.UOMobileData;
import com.github.mayconr.juoserver.game.model.UONpc;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.model.event.MobileDeathEvent;
import com.github.mayconr.juoserver.game.world.context.FlowRegistryFactory.GameInfra;
import com.github.mayconr.juoserver.game.world.context.FlowRegistryFactory.GameModules;
import com.github.mayconr.juoserver.game.world.context.FlowRegistryFactory.GameTemplates;
import java.util.List;
import org.junit.jupiter.api.Test;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DeathLootTest {
    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void deathNotifiesAfterLootEvenWhenGenerationFails(boolean failLoot) {
        var npc = new UONpc(new UOMobileData());
        npc.setAlive(true);
        var killer = new UOPlayer(new UOMobileData());
        var corpseData = new UOItemData();
        corpseData.setLocationType(ItemLocationType.GROUND);
        var corpse = new UOCorpse(corpseData, mock(ItemTemplate.class));
        var items = mock(ItemModule.class);
        when(items.tryCreateItem(any(), any(), any())).thenReturn(new ItemCreationResult.Created(corpse));
        var loot = mock(LootModule.class);
        when(loot.generate(any())).thenAnswer(call -> {
            assertFalse(npc.isAlive());
            assertSame(corpse, ((LootRequest) call.getArgument(0)).corpse());
            if (failLoot) throw new IllegalStateException("loot failed");
            return List.of();
        });
        var bus = mock(EventBus.class);
        var result = DeathFlowDefinition.build(GameModules.builder().item(items).loot(loot).build(),
                GameInfra.builder().eventBus(bus).build(), GameTemplates.builder().build())
                .execute(new DeathContext(npc, killer, DeathCause.COMBAT));
        assertTrue(result.flowSucceeded());
        var order = inOrder(loot, bus);
        order.verify(loot).generate(any());
        order.verify(bus).publish(new MobileDeathEvent(killer, npc, DeathCause.COMBAT, corpse));
    }

    @Test
    void playerDeathsDoNotInvokeLootPolicy() {
        var loot = mock(LootModule.class);
        var context = new DeathContext(new UOPlayer(new UOMobileData()), null, DeathCause.COMBAT);
        var result = new GenerateDeathLootStep(loot)
                .execute(context);
        assertTrue(result.flowSucceeded());
        verifyNoInteractions(loot);
    }
}
