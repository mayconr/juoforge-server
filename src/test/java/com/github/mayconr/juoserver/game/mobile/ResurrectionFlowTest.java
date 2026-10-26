package com.github.mayconr.juoserver.game.mobile;

import com.github.mayconr.juoserver.game.mobile.flow.resurrection.*;
import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.game.model.event.MobileResurrectEvent;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ResurrectionFlowTest {
    private final EventBus events = mock(EventBus.class);

    private UOPlayer player(int hp, int stamina, int mana) {
        var data = new UOMobileData();
        data.setMaxHitpoints(hp);
        data.setMaxStamina(stamina);
        data.setMaxMana(mana);
        return new UOPlayer(data);
    }

    @ParameterizedTest
    @CsvSource({"100, 100, 100, 5, 5, 5", "3, 2, 1, 3, 2, 1", "1, 0, 0, 1, 0, 0"})
    void restoresLifeWithinVitalsLimitsBeforeNotifying(int maxHp, int maxStamina, int maxMana,
                                                     int hp, int stamina, int mana) {
        var player = player(maxHp, maxStamina, maxMana);
        doAnswer(call -> {
            assertTrue(player.isAlive());
            assertEquals(hp, player.getHitpoints());
            assertEquals(stamina, player.getStamina());
            assertEquals(mana, player.getMana());
            return null;
        }).when(events).publish(new MobileResurrectEvent(player));

        var result = ResurrectionFlowDefinition.build(events).execute(new ResurrectionContext(player));

        assertTrue(result.flowSucceeded());
        verify(events).publish(new MobileResurrectEvent(player));
    }

    @Test
    void rejectsMissingTarget() {
        var result = ResurrectionFlowDefinition.build(events).execute(new ResurrectionContext(null));
        assertEquals("MISSING_MOBILE", result.code());
        verifyNoInteractions(events);
    }

    @Test
    void rejectsNpcWithoutChangingItsState() {
        var npc = new UONpc(new UOMobileData());
        var result = ResurrectionFlowDefinition.build(events).execute(new ResurrectionContext(npc));
        assertEquals("NOT_PLAYER", result.code());
        assertFalse(npc.isAlive());
        verifyNoInteractions(events);
    }

    @Test
    void secondResurrectionDoesNotResetVitalsOrPublishAgain() {
        var player = player(100, 100, 100);
        var flow = ResurrectionFlowDefinition.build(events);
        flow.execute(new ResurrectionContext(player));
        player.setHitpoints(20);

        var result = flow.execute(new ResurrectionContext(player));

        assertEquals("ALREADY_ALIVE", result.code());
        assertEquals(20, player.getHitpoints());
        verify(events, times(1)).publish(new MobileResurrectEvent(player));
    }

    @ParameterizedTest
    @CsvSource({"0, 100, 100", "-1, 100, 100", "100, -1, 100", "100, 100, -1"})
    void rejectsInvalidMaximumVitals(int hp, int stamina, int mana) {
        var player = player(hp, stamina, mana);

        var result = ResurrectionFlowDefinition.build(events).execute(new ResurrectionContext(player));

        assertEquals("INVALID_VITALS", result.code());
        assertFalse(player.isAlive());
        assertEquals(0, player.getHitpoints());
        verifyNoInteractions(events);
    }
}
