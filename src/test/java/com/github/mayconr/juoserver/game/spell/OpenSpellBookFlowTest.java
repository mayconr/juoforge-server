package com.github.mayconr.juoserver.game.spell;

import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.game.model.event.SpellBookOpened;
import com.github.mayconr.juoserver.game.spell.flow.open.*;
import com.github.mayconr.juoserver.game.spell.template.SpellTemplate;
import com.github.mayconr.juoserver.game.spell.trigger.SpellCastRegistry;
import com.github.mayconr.juoserver.game.world.context.*;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;
import com.github.mayconr.juoserver.infrastructure.template.InMemoryTemplateRegistry;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OpenSpellBookFlowTest {
    private final EventBus events = mock(EventBus.class);
    private final RealmStorage storage = mock(RealmStorage.class);
    private final UOPlayer player = mock(UOPlayer.class);
    private final UOItem book = mock(UOItem.class);
    private final com.github.mayconr.juoserver.infrastructure.flow.Flow<OpenSpellBookContext> flow =
            OpenSpellBookFlowDefinition.build(FlowRegistryFactory.GameInfra.builder()
                    .eventBus(events).storage(storage).build());

    OpenSpellBookFlowTest() {
        when(player.getSerialId()).thenReturn(1);
        when(book.getSerialId()).thenReturn(0x40000001);
        when(book.getModelId()).thenReturn(0x0EFA);
        when(book.getCurrentLocation()).thenReturn(ItemLocation.equipped(1));
    }

    private OpenSpellBookContext context() {
        return new OpenSpellBookContext(player, book, SpellbookType.MAGERY, -1L);
    }

    @Test
    void moduleExecutesRegisteredFlowAndPublishesDomainEvent() {
        var registry = new DefaultFlowRegistry();
        registry.register("OpenSpellBook", flow, OpenSpellBookContext.class);
        var module = new SpellModuleImpl(
                new InMemoryTemplateRegistry<String, SpellTemplate>(List.of(), SpellTemplate::key));
        module.initialize(DefaultModuleContext.builder().flowFacade(new DefaultFlowFacade(registry)).build());
        module.openSpellBook(player, book, SpellbookType.MAGERY, -1L);
        verify(events).publish(new SpellBookOpened(player, 0x40000001, 0x0EFA, SpellbookType.MAGERY, -1L));
    }

    @Test
    void allowsBookInsidePlayersContainerDespiteContainerCoordinates() {
        var bag = mock(UOItem.class);
        when(book.getCurrentLocation()).thenReturn(ItemLocation.container(0x40000002));
        when(book.getX()).thenReturn(999);
        when(bag.getSerialId()).thenReturn(0x40000002);
        when(bag.getCurrentLocation()).thenReturn(ItemLocation.equipped(1));
        when(storage.getItem(0x40000002)).thenReturn(Optional.of(bag));
        assertTrue(flow.execute(context()).flowSucceeded());
        verify(events).publish(any(SpellBookOpened.class));
    }

    @Test
    void rejectsInvalidRequestsBeforeNotification() {
        assertTrue(flow.execute(new OpenSpellBookContext(null, book, SpellbookType.MAGERY, 0)).flowFailed());
        assertTrue(flow.execute(new OpenSpellBookContext(player, null, SpellbookType.MAGERY, 0)).flowFailed());
        assertTrue(flow.execute(new OpenSpellBookContext(player, book, null, 0)).flowFailed());
        when(book.getModelId()).thenReturn(0x10000);
        assertTrue(flow.execute(context()).flowFailed());
        verifyNoInteractions(events);
    }

    @Test
    void rejectsOtherOwnersOrphansMissingContainersAndCycles() {
        when(book.getCurrentLocation()).thenReturn(ItemLocation.equipped(2));
        assertTrue(flow.execute(context()).flowFailed());
        when(book.getCurrentLocation()).thenReturn(ItemLocation.orphan());
        assertTrue(flow.execute(context()).flowFailed());
        when(book.getCurrentLocation()).thenReturn(ItemLocation.container(0x40000002));
        assertTrue(flow.execute(context()).flowFailed());
        when(book.getCurrentLocation()).thenReturn(ItemLocation.container(0x40000001));
        when(storage.getItem(book.getSerialId())).thenReturn(Optional.of(book));
        assertTrue(flow.execute(context()).flowFailed());
        verifyNoInteractions(events);
    }

    @Test
    void groundBookMustBeWithinInteractionRange() {
        when(book.getCurrentLocation()).thenReturn(ItemLocation.ground());
        when(book.getX()).thenReturn(3);
        assertTrue(flow.execute(context()).flowFailed());
        verifyNoInteractions(events);
        when(book.getX()).thenReturn(2);
        assertTrue(flow.execute(context()).flowSucceeded());
        verify(events).publish(any(SpellBookOpened.class));
    }
}
