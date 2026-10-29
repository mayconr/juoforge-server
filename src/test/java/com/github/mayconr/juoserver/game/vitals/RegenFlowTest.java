package com.github.mayconr.juoserver.game.vitals;

import com.github.mayconr.juoserver.game.GamePlaySettings;
import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.game.model.event.VitalsChanged;
import com.github.mayconr.juoserver.game.vitals.flow.regen.*;
import com.github.mayconr.juoserver.game.world.context.FlowRegistryFactory;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RegenFlowTest {
    private final EventBus bus = mock(EventBus.class);
    private final com.github.mayconr.juoserver.game.ui.WorldUI ui = mock(com.github.mayconr.juoserver.game.ui.WorldUI.class);
    private final GamePlaySettings settings = mock(GamePlaySettings.class, RETURNS_DEEP_STUBS);

    private com.github.mayconr.juoserver.infrastructure.flow.Flow<RegenContext> flow() {
        when(settings.mobile().vitals().saturationFactor()).thenReturn(100);
        return RegenFlowDefinition.build(settings, bus, ui);
    }

    private UOPlayer player() {
        var mobile = new UOPlayer(new UOMobileData());
        mobile.setAlive(true);
        mobile.setConnected(true);
        mobile.setHitpoints(50);
        mobile.setMana(50);
        mobile.setStamina(50);
        mobile.setMaxHitpoints(100);
        mobile.setMaxMana(100);
        mobile.setMaxStamina(100);
        mobile.setStrength(100);
        mobile.setDexterity(100);
        mobile.setIntelligence(100);
        return mobile;
    }

    @Test
    void missingMobileFailsBeforeCalculationOrNotification() {
        var result = flow().execute(new RegenContext(null, 10));
        assertTrue(result.flowFailed());
        assertEquals("MISSING_MOBILE", result.code());
        verifyNoInteractions(bus, ui);
    }

    @ParameterizedTest
    @ValueSource(doubles = {-1, Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY})
    void invalidIntervalsLeaveResourcesAndAccumulatorsUntouched(double interval) {
        var mobile = player();
        var result = flow().execute(new RegenContext(mobile, interval));
        assertEquals("INVALID_INTERVAL", result.code());
        assertEquals(50, mobile.getHitpoints());
        assertTrue(mobile.runtimeAttributes().toMap().isEmpty());
        verifyNoInteractions(bus, ui);
    }

    @Test
    void invalidSettingsFailWithoutMutation() {
        var flow = flow();
        when(settings.mobile().vitals().saturationFactor()).thenReturn(0);
        var mobile = player();
        var result = flow.execute(new RegenContext(mobile, 10));
        assertEquals("INVALID_REGEN_SETTINGS", result.code());
        assertEquals(50, mobile.getHitpoints());
        assertTrue(mobile.runtimeAttributes().toMap().isEmpty());
        verifyNoInteractions(bus, ui);
    }

    @Test
    void ineligibleAndZeroIntervalStopTheFlowBeforeCalculation() {
        var flow = flow();
        var mobile = player();
        var zero = new RegenContext(mobile, 0);
        assertEquals("ZERO_INTERVAL", flow.execute(zero).code());
        assertNull(zero.getHitpoints());
        mobile.setAlive(false);
        mobile.runtimeAttributes().set(RegenContext.HP_ACCUMULATOR, 0.5);
        var dead = new RegenContext(mobile, 10);
        assertEquals("INELIGIBLE_MOBILE", flow.execute(dead).code());
        assertNull(dead.getHitpoints());
        assertTrue(mobile.runtimeAttributes().toMap().isEmpty());
        mobile.setAlive(true);
        mobile.setConnected(false);
        assertEquals("INELIGIBLE_MOBILE", flow.execute(new RegenContext(mobile, 10)).code());
        verifyNoInteractions(bus, ui);
    }

    @Test
    void calculationDoesNotMutateAndNotificationSeesAllAppliedResources() {
        when(settings.mobile().vitals().saturationFactor()).thenReturn(100);
        var mobile = player();
        var context = new RegenContext(mobile, 10);
        new CalculateRegenStep(settings).execute(context);
        assertEquals(56, context.getHitpoints().next());
        assertEquals(50, mobile.getHitpoints());
        assertTrue(mobile.runtimeAttributes().toMap().isEmpty());
        doAnswer(call -> {
            assertEquals(56, mobile.getHitpoints());
            assertEquals(56, mobile.getStamina());
            assertEquals(56, mobile.getMana());
            return null;
        }).when(bus).publish(new VitalsChanged(mobile));
        assertTrue(flow().execute(new RegenContext(mobile, 10)).flowSucceeded());
        verify(bus, times(1)).publish(new VitalsChanged(mobile));
        verify(ui).sendStatusGump(mobile, mobile.getSerialId());
    }

    @Test
    void eachResourceUsesItsOwnStatAndNegativeStatsUseMinimumRate() {
        var mobile = player();
        mobile.setStrength(-100);
        mobile.setDexterity(0);
        assertTrue(flow().execute(new RegenContext(mobile, 10)).flowSucceeded());
        assertEquals(51, mobile.getHitpoints());
        assertEquals(51, mobile.getStamina());
        assertEquals(56, mobile.getMana());
    }

    @Test
    void factoryRegistersTheRegenContextInTheCoreRegistry() {
        var infra = mock(FlowRegistryFactory.GameInfra.class, RETURNS_DEEP_STUBS);
        var modules = mock(FlowRegistryFactory.GameModules.class, RETURNS_DEEP_STUBS);
        var templates = mock(FlowRegistryFactory.GameTemplates.class, RETURNS_DEEP_STUBS);
        when(infra.settings()).thenReturn(settings);
        when(infra.eventBus()).thenReturn(bus);
        when(settings.mobile().vitals().saturationFactor()).thenReturn(100);
        var registry = FlowRegistryFactory.builder().infra(infra).modules(modules).templates(templates)
                .build().buildRegistry();
        var registered = registry.get(RegenContext.class);
        assertNotNull(registered);
        var mobile = player();
        assertTrue(registered.execute(new RegenContext(mobile, 10)).flowSucceeded());
        assertEquals(56, mobile.getHitpoints());
    }
}
