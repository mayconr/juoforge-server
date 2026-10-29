package com.github.mayconr.juoserver.game.skill;

import com.github.mayconr.juoserver.game.GamePlaySettings;
import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.game.model.event.SkillGained;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.rng.RNG;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TryGainSkillFlowTest {
    private final RNG rng = mock(RNG.class);
    private final EventBus events = mock(EventBus.class);
    private final UOMobile mobile = mock(UOMobile.class);
    private final SkillGainCalculator calculator = new SkillGainCalculator(new GamePlaySettings().skills());
    private final SkillModuleImpl module = createModule();

    private SkillModuleImpl createModule() {
        var registry = new com.github.mayconr.juoserver.game.world.context.DefaultFlowRegistry();
        registry.register("try-gain",
                com.github.mayconr.juoserver.game.skill.flow.gain.TryGainSkillFlowDefinition.build(calculator, rng, events),
                com.github.mayconr.juoserver.game.skill.flow.gain.TryGainSkillContext.class);
        var result = new SkillModuleImpl(null);
        result.initialize(new com.github.mayconr.juoserver.game.world.context.DefaultModuleContext(
                new com.github.mayconr.juoserver.game.world.context.DefaultFlowFacade(registry)));
        return result;
    }

    @Test
    void failedRollDoesNotChangeSkillOrNotify() {
        var skill = prepare(40, 100, SkillLock.UP);
        module.tryGain(mobile, 1, 50, SkillGainContext.of(mobile));
        assertEquals(40, skill.getBase());
        verify(rng).roll(0.5);
        verifyNoInteractions(events);
    }

    @Test
    void accumulatesFractionalGainsAndNotifiesOnlyAfterVisibleTenthIncreases() {
        var skill = prepare(40.0995, 100, SkillLock.UP);
        when(rng.roll(anyDouble())).thenReturn(true);
        module.tryGain(mobile, 1, 50, SkillGainContext.of(mobile));
        assertTrue(skill.getBase() > 40.0995 && skill.getBase() < 40.1);
        verifyNoInteractions(events);
        module.tryGain(mobile, 1, 50, SkillGainContext.of(mobile));
        assertTrue(skill.getBase() >= 40.1 && skill.getBase() < 40.2);
        verify(events).publish(new SkillGained(mobile, skill));
    }

    @ParameterizedTest
    @EnumSource(value = SkillLock.class, names = {"DOWN", "LOCKED"})
    void respectsSkillLocks(SkillLock lock) {
        var skill = prepare(40, 100, lock);
        when(rng.roll(anyDouble())).thenReturn(true);
        module.tryGain(mobile, 1, 50, SkillGainContext.of(mobile));
        assertEquals(40, skill.getBase());
        verifyNoInteractions(events);
    }

    @Test
    void gainStopsAtCapAndEventSeesUpdatedValue() {
        var skill = prepare(49.99999, 50, SkillLock.UP);
        when(rng.roll(anyDouble())).thenReturn(true);
        doAnswer(call -> {
            assertEquals(50, skill.getBase());
            return null;
        }).when(events).publish(any(SkillGained.class));
        module.tryGain(mobile, 1, 50, SkillGainContext.of(mobile));
        assertEquals(50, skill.getBase());
        verify(events).publish(new SkillGained(mobile, skill));
    }

    @Test
    void formulasPreserveChanceLimitsAndBeginnerBonus() {
        assertEquals(0, calculator.calculateChance(SkillValue.of(1, 100, 100), 0));
        assertEquals(0.5, calculator.calculateChance(SkillValue.of(1, 0, 100), 100));
        assertEquals(0.00113654 * 19.77, calculator.calculateGainAmount(SkillValue.of(1, 0, 100), 100), 1e-9);
        assertEquals(0, calculator.calculateGainAmount(SkillValue.of(1, 0, 0), 100));
        var skill = SkillValue.of(1, 40, 100);
        assertTrue(calculator.calculateGainAmount(skill, 150) >= calculator.calculateGainAmount(skill, 50));
    }

    @Test
    void uninitializedSkillUsesContainerDefaults() {
        var skills = new SkillContainer();
        when(mobile.getSkills()).thenReturn(skills);
        module.tryGain(mobile, 1, 50, SkillGainContext.of(mobile));
        assertEquals(0, skills.get(1).getBase());
        assertEquals(100, skills.get(1).getCap());
        verify(rng).roll(0.5);
        verifyNoInteractions(events);
    }

    @Test
    void defaultProgressionTargetsThreeHoursThenSixDays() {
        var skill = SkillValue.of(1, 0, 100);
        double expectedSeconds = 0;
        double secondsToThirty = 0;
        int gains = 0;
        while (skill.getBase() < 100 && gains++ < 300_000) {
            expectedSeconds += 1.0 / calculator.calculateChance(skill, 100);
            skill.increase(calculator.calculateGainAmount(skill, 100));
            if (secondsToThirty == 0 && skill.getBase() >= 30) {
                secondsToThirty = expectedSeconds;
            }
        }
        assertEquals(100, skill.getBase());
        assertEquals(3 * 3600, secondsToThirty, 60);
        assertEquals(6 * 86400, expectedSeconds - secondsToThirty, 60);
        var atThirty = SkillValue.of(1, 30, 100);
        assertEquals(0.00113654 * 0.7, calculator.calculateGainAmount(atThirty, 100), 1e-12);
    }

    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.ValueSource(doubles = {0, 15, 29.99, 30, 50, 90})
    void beginnerBonusAppliesOnlyBelowThirtyThroughTheFlow(double initialBase) {
        var defaults = new GamePlaySettings().skills();
        var withoutBonus = new GamePlaySettings.Skills(
                defaults.minGainChance(), defaults.maxGainChance(), defaults.balanceOffset(),
                defaults.cap(), 1.0, defaults.beginnerGainThreshold(), defaults.baseGain());
        var skill = prepare(initialBase, 100, SkillLock.UP);
        when(rng.roll(anyDouble())).thenReturn(true);

        module.tryGain(mobile, 1, 100, SkillGainContext.of(mobile));
        double boostedGain = skill.getBase() - initialBase;

        skill.setBase(initialBase);
        var flow = com.github.mayconr.juoserver.game.skill.flow.gain.TryGainSkillFlowDefinition.build(
                new SkillGainCalculator(withoutBonus), rng, events);
        var result = flow.execute(new com.github.mayconr.juoserver.game.skill.flow.gain.TryGainSkillContext(
                mobile, 1, 100, SkillGainContext.of(mobile)));
        assertTrue(result.flowSucceeded());
        double ordinaryGain = skill.getBase() - initialBase;

        assertTrue(ordinaryGain > 0);
        if (initialBase < 30) {
            assertTrue(boostedGain > ordinaryGain, "Beginner bonus must increase the actual gain below 30");
        } else {
            assertEquals(ordinaryGain, boostedGain, 1e-12, "No beginner bonus may remain at or above 30");
        }
    }

    @Test
    void repeatedSuccessfulAttemptsProgressFromZeroThroughThirtyToCap() {
        var skill = prepare(0, 100, SkillLock.UP);
        when(rng.roll(anyDouble())).thenReturn(true);
        var gainContext = SkillGainContext.of(mobile);
        int attempts = 0;
        int attemptsToThirty = 0;
        double previousGainAfterThirty = Double.POSITIVE_INFINITY;

        while (skill.getBase() < 100 && attempts < 300_000) {
            double previousBase = skill.getBase();
            module.tryGain(mobile, 1, 100, gainContext);
            attempts++;
            double currentBase = skill.getBase();
            double gained = currentBase - previousBase;
            assertTrue(gained > 0, "Every successful attempt must advance the skill");
            assertTrue(currentBase <= 100, "Progression must respect the cap");
            if (previousBase >= 30) {
                assertTrue(gained <= previousGainAfterThirty + 1e-12,
                        "Post-bonus gains must remain positive without increasing near the cap");
                previousGainAfterThirty = gained;
            }
            if (attemptsToThirty == 0 && currentBase >= 30) attemptsToThirty = attempts;
            // Keep Mockito's invocation history bounded during the complete progression.
            if (attempts % 1000 == 0) clearInvocations(rng, events, mobile);
        }

        assertEquals(100, skill.getBase(), "Repeated attempts must eventually reach the cap");
        assertTrue(attemptsToThirty > 0);
        assertTrue(attempts - attemptsToThirty > attemptsToThirty,
                "The post-bonus range should require more successful attempts");
        clearInvocations(events);
        module.tryGain(mobile, 1, 100, gainContext);
        assertEquals(100, skill.getBase());
        verifyNoInteractions(events);
    }

    private SkillValue prepare(double base, double cap, SkillLock lock) {
        var skill = SkillValue.of(1, base, cap, lock);
        when(mobile.getSkills()).thenReturn(new SkillContainer(List.of(skill)));
        return skill;
    }
}
