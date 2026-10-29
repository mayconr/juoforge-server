package com.github.mayconr.juoserver.game.vitals;

import com.github.mayconr.juoserver.game.GamePlaySettings;
import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.game.model.event.VitalsChanged;
import com.github.mayconr.juoserver.game.player.WorldPlayer;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.game.world.context.DefaultFlowRegistry;
import com.github.mayconr.juoserver.game.world.context.DefaultFlowFacade;
import com.github.mayconr.juoserver.game.world.context.ModuleContext;
import com.github.mayconr.juoserver.game.world.ModuleOperationException;
import com.github.mayconr.juoserver.game.vitals.flow.regen.RegenContext;
import com.github.mayconr.juoserver.game.vitals.flow.regen.RegenFlowDefinition;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class VitalsModuleTest {
    private final EventBus bus = mock(EventBus.class);
    private final com.github.mayconr.juoserver.game.ui.WorldUI ui = mock(com.github.mayconr.juoserver.game.ui.WorldUI.class);
    private final WorldPlayer players = mock(WorldPlayer.class);

    private VitalsModuleImpl module() {
        var settings = mock(GamePlaySettings.class, RETURNS_DEEP_STUBS);
        when(settings.vitals().saturationFactor()).thenReturn(100);
        var registry = new DefaultFlowRegistry();
        registry.register("regen", RegenFlowDefinition.build(settings, bus, ui),
                RegenContext.class);
        var context = mock(ModuleContext.class);
        when(context.flows()).thenReturn(new DefaultFlowFacade(registry));
        var module = new VitalsModuleImpl(players);
        module.initialize(context);
        return module;
    }

    private UOPlayer player(int value) {
        var data = new UOMobileData();
        data.setPersistentAttrMap(new DefaultAttributeMap());
        var player = new UOPlayer(data);
        player.setConnected(true);
        player.setAlive(true);
        player.setStrength(100);
        player.setDexterity(100);
        player.setIntelligence(100);
        player.setMaxHitpoints(100);
        player.setMaxMana(100);
        player.setMaxStamina(100);
        player.setHitpoints(value);
        player.setMana(value);
        player.setStamina(value);
        return player;
    }

    @Test
    void worldUpdateUsesAllElapsedTimeAndPublishesOneChangePerMobile() {
        var player = player(50);
        when(players.getOnlinePlayers()).thenReturn(List.of(player));
        module().update(10);
        assertEquals(56, player.getHitpoints());
        assertEquals(56, player.getMana());
        assertEquals(56, player.getStamina());
        verify(bus).publish(new VitalsChanged(player));
        verify(ui).sendStatusGump(player, player.getSerialId());
    }

    @Test
    void fractionalTicksProduceTheSameGainAsOneLongTick() {
        var smallTicks = player(50);
        var largeTick = player(50);
        var module = module();
        for (int i = 0; i < 10; i++) {
            module.regen(smallTicks, 1);
        }
        module.regen(largeTick, 10);
        assertEquals(56, smallTicks.getHitpoints());
        assertEquals(largeTick.getHitpoints(), smallTicks.getHitpoints());
        assertEquals(largeTick.getMana(), smallTicks.getMana());
        assertEquals(largeTick.getStamina(), smallTicks.getStamina());
    }

    @Test
    void capsGainAndDoesNotBankRegenerationWhileFull() {
        var player = player(98);
        var module = module();
        module.regen(player, 10);
        assertEquals(100, player.getHitpoints());
        assertEquals(100, player.getMana());
        assertEquals(100, player.getStamina());
        module.regen(player, 1000);
        player.setHitpoints(90);
        module.regen(player, 1);
        assertEquals(90, player.getHitpoints());
        module.regen(player, 1);
        assertEquals(91, player.getHitpoints());
    }

    @Test
    void skipsDeadAndOfflinePlayersAndDiscardsTheirFractionalProgress() {
        var player = player(50);
        var module = module();
        module.regen(player, 1);
        player.setAlive(false);
        module.regen(player, 100);
        player.setAlive(true);
        player.setConnected(false);
        module.regen(player, 100);
        assertEquals(50, player.getHitpoints());
        player.setConnected(true);
        module.regen(player, 1);
        assertEquals(50, player.getHitpoints());
        verifyNoInteractions(bus);
    }

    @Test
    void keepsAccumulatorsInRuntimeStateAndEmitsNothingWithoutAResourceChange() {
        var player = player(50);
        var module = module();
        module.regen(player, 0.1);
        module.regen(player, 0);
        assertEquals(50, player.getHitpoints());
        assertFalse(player.runtimeAttributes().toMap().isEmpty());
        assertTrue(player.persistentAttributes().toMap().isEmpty());
        verifyNoInteractions(bus);
        assertThrows(IllegalArgumentException.class, () -> module.regen(player, -1));
        assertThrows(IllegalArgumentException.class, () -> module.update(Double.NaN));
    }

    @Test
    void explicitRegenerationAlsoSupportsLivingNpcs() {
        var npc = new UONpc(new UOMobileData());
        npc.setAlive(true);
        npc.setStrength(100);
        npc.setMaxHitpoints(100);
        npc.setHitpoints(50);
        module().regen(npc, 10);
        assertEquals(56, npc.getHitpoints());
        verify(bus).publish(new VitalsChanged(npc));
    }

    @Test
    void propagatesFlowFailureWithItsCodeAndCause() {
        var facade = mock(ModuleContext.FlowFacade.class);
        var context = mock(ModuleContext.class);
        when(context.flows()).thenReturn(facade);
        var cause = new IllegalStateException("calculation failed");
        when(facade.execute(any(RegenContext.class))).thenReturn(
                com.github.mayconr.juoserver.infrastructure.flow.StepResult.failure(
                        "REGEN_FAILED", "Regeneration failed", null, cause));
        var module = new VitalsModuleImpl(players);
        module.initialize(context);
        var failure = assertThrows(ModuleOperationException.class, () -> module.regen(player(50), 1));
        assertEquals("regen", failure.getOperation());
        assertEquals("REGEN_FAILED", failure.getCode());
        assertSame(cause, failure.getCause());
    }
}
