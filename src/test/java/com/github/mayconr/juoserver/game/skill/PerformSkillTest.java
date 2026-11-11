package com.github.mayconr.juoserver.game.skill;

import com.github.mayconr.juoforge.reader.skill.Skill;
import com.github.mayconr.juoserver.game.messaging.WorldMessage;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.model.UOMobileData;
import com.github.mayconr.juoserver.game.model.DefaultAttributeMap;
import com.github.mayconr.juoserver.game.mobile.MobileModuleImpl;
import com.github.mayconr.juoserver.game.mobile.WorldMobile;
import com.github.mayconr.juoserver.game.model.event.message.MessageContent;
import com.github.mayconr.juoserver.game.skill.flow.perform.PerformSkillContext;
import com.github.mayconr.juoserver.game.skill.flow.update.UpdateSkillActionsContext;
import com.github.mayconr.juoserver.game.skill.flow.update.UpdateSkillActionsFlowDefinition;
import com.github.mayconr.juoserver.game.skill.flow.swings.SetSkillSwingCountContext;
import com.github.mayconr.juoserver.game.skill.flow.swings.SetSkillSwingCountFlowDefinition;
import com.github.mayconr.juoserver.game.skill.flow.perform.PerformSkillFlowDefinition;
import com.github.mayconr.juoserver.game.world.ModuleOperationException;
import com.github.mayconr.juoserver.game.world.context.DefaultFlowFacade;
import com.github.mayconr.juoserver.game.world.context.DefaultFlowRegistry;
import com.github.mayconr.juoserver.game.world.context.DefaultModuleContext;
import com.github.mayconr.juoserver.infrastructure.datafile.UOFileReader;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class PerformSkillTest {
    private final WorldMessage messages = mock(WorldMessage.class);
    private final WorldMobile mobiles = new MobileModuleImpl(null, null, null, null, null);

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void acceptsExistingSkillWithoutActionAndRejectsMissingSkill(boolean exists) {
        var reader = mock(UOFileReader.class);
        var skills = mock(SkillModule.class);
        when(reader.getSkill(1)).thenReturn(exists ? Optional.of(mock(Skill.class)) : Optional.empty());
        var player = player(1);
        when(skills.reserveSkillAction(eq(player), any())).thenReturn(true);
        var context = new PerformSkillContext(player, options(count -> {}));

        var result = PerformSkillFlowDefinition.build(reader, skills, messages, mobiles).execute(context);

        if (exists) {
            assertTrue(result.flowSucceeded());
            assertTrue(context.result());
            verify(skills).reserveSkillAction(eq(player), any(SkillAction.class));
        } else {
            assertEquals("SKILL_NOT_FOUND", result.code());
            verify(skills, never()).reserveSkillAction(any(), any());
        }
        verifyNoInteractions(messages);
    }

    @Test
    void singleSwingRunsBeforeDelayAndCompletesExactlyOnce() {
        var module = module();
        var player = player(1);
        var swing = mock(SkillCallback.class);
        var completion = mock(SkillCallback.class);
        assertTrue(module.performSkill(player, SkillPerformOptions.builder()
                .skillId(1).swing(swing).onComplete(completion).build()));
        assertTrue(module.hasActiveAction(player));
        verifyNoInteractions(swing, completion);

        module.update(0);
        assertFalse(module.hasActiveAction(player));
        module.update(1);

        var order = inOrder(swing, completion);
        order.verify(swing).execute(1);
        order.verify(completion).execute(1);
        verifyNoMoreInteractions(swing, completion);
    }

    @Test
    void firstSwingRunsBeforeDelayAndLaterSwingsWaitOneSecond() {
        var module = module();
        var player = player(1);
        var swing = mock(SkillCallback.class);
        assertTrue(module.performSkill(player, SkillPerformOptions.builder()
                .skillId(1).swingCount(2).swing(swing).build()));

        module.update(0);
        verify(swing).execute(1);
        assertTrue(module.hasActiveAction(player));
        module.update(0.5);
        verify(swing, never()).execute(2);
        assertTrue(module.hasActiveAction(player));
        module.update(0.5);
        module.update(100);

        verify(swing).execute(1);
        verify(swing).execute(2);
        verifyNoMoreInteractions(swing);
        assertFalse(module.hasActiveAction(player));
    }

    @Test
    void movingBeforeTheFirstSwingCancelsAllSwingsAndCompletesOnce() {
        var module = module();
        var player = player(1);
        var swing = mock(SkillCallback.class);
        var completion = mock(SkillCallback.class);
        module.performSkill(player, SkillPerformOptions.builder()
                .skillId(1).swingCount(3).swing(swing).onComplete(completion).build());

        player.setLocation(1, 0);
        module.update(0);
        module.update(100);

        assertFalse(module.hasActiveAction(player));
        verifyNoInteractions(swing);
        verify(completion).execute(0);
        verifyNoMoreInteractions(completion);
    }

    @Test
    void movingBetweenSwingsCancelsTheRemainingSwings() {
        var module = module();
        var player = player(1);
        var swing = mock(SkillCallback.class);
        var completion = mock(SkillCallback.class);
        module.performSkill(player, SkillPerformOptions.builder()
                .skillId(1).swingCount(3).swing(swing).onComplete(completion).build());

        module.update(0);
        player.setLocation(0, 1);
        module.update(1);
        module.update(1);

        assertFalse(module.hasActiveAction(player));
        var order = inOrder(swing, completion);
        order.verify(swing).execute(1);
        order.verify(completion).execute(1);
        verifyNoMoreInteractions(swing, completion);
    }

    @Test
    void leavingTheOriginBetweenSwingsCancelsEvenAfterReturning() {
        var module = module();
        var player = player(1);
        var swing = mock(SkillCallback.class);
        var completion = mock(SkillCallback.class);
        module.performSkill(player, SkillPerformOptions.builder()
                .skillId(1).swingCount(2).swing(swing).onComplete(completion).build());

        module.update(0);
        player.setLocation(1, 1);
        module.update(0.5);
        player.setLocation(0, 0);
        module.update(0.5);

        verify(swing).execute(1);
        verify(swing, never()).execute(2);
        verify(completion).execute(1);
        assertFalse(module.hasActiveAction(player));
    }

    @Test
    void changingOnlyHeightDoesNotCancelTheAction() {
        var module = module();
        var player = player(1);
        var swing = mock(SkillCallback.class);
        module.performSkill(player, SkillPerformOptions.builder()
                .skillId(1).swingCount(2).swing(swing).build());

        module.update(0);
        player.setZ(5);
        module.update(1);

        verify(swing).execute(1);
        verify(swing).execute(2);
    }

    @Test
    void failedCompletionAfterMovementStillReleasesTheAction() {
        var module = module();
        var player = player(1);
        var swing = mock(SkillCallback.class);
        module.performSkill(player, SkillPerformOptions.builder()
                .skillId(1)
                .swingCount(2)
                .swing(swing)
                .onComplete(count -> { throw new IllegalStateException("failed completion"); })
                .build());

        player.setLocation(1, 0);
        module.update(0);

        assertFalse(module.hasActiveAction(player));
        verifyNoInteractions(swing);
    }

    @Test
    void busyActionIsNotReplacedAndNotifiesPlayer() {
        var module = module();
        var player = player(1);
        var original = mock(SkillCallback.class);
        var rejected = mock(SkillCallback.class);
        assertTrue(module.performSkill(player, options(original)));
        assertFalse(module.performSkill(player, options(rejected)));

        module.update(1);

        verify(messages).send(player, MessageContent.localized("skill.action.already_active"));
        verify(original).execute(1);
        verifyNoInteractions(rejected);
    }

    @Test
    void failedSwingReleasesActionWithoutCallingCompletion() {
        var module = module();
        var player = player(1);
        var swing = mock(SkillCallback.class);
        var completion = mock(SkillCallback.class);
        doThrow(new IllegalStateException("failed")).when(swing).execute(1);
        module.performSkill(player, SkillPerformOptions.builder()
                .skillId(1).swing(swing).onComplete(completion).build());

        module.update(1);

        assertFalse(module.hasActiveAction(player));
        verifyNoInteractions(completion);
    }

    @Test
    void completionCanScheduleAnotherActionForTheNextUpdate() {
        var module = module();
        var player = player(1);
        var next = mock(SkillCallback.class);
        module.performSkill(player, SkillPerformOptions.builder()
                .skillId(1)
                .swing(count -> {})
                .onComplete(count -> assertTrue(module.performSkill(player, options(next))))
                .build());

        module.update(1);
        assertTrue(module.hasActiveAction(player));
        verifyNoInteractions(next);
        module.update(1);
        verify(next).execute(1);
        assertFalse(module.hasActiveAction(player));
    }

    @ParameterizedTest
    @ValueSource(doubles = {-1, Double.NaN, Double.POSITIVE_INFINITY})
    void invalidUpdateDeltaDoesNotAdvanceOrReleaseActions(double delta) {
        var module = module();
        var player = player(1);
        var swing = mock(SkillCallback.class);
        module.performSkill(player, SkillPerformOptions.builder()
                .skillId(1).swing(swing).build());

        var error = assertThrows(ModuleOperationException.class, () -> module.update(delta));

        assertEquals("INVALID_SKILL_ACTION_DELTA", error.getCode());
        assertTrue(module.hasActiveAction(player));
        verifyNoInteractions(swing);
        module.update(1);
        verify(swing).execute(1);
    }

    @Test
    void oneFailedActionDoesNotPreventOtherPlayersFromRunning() {
        var module = module();
        var first = player(1);
        var second = player(2);
        var successful = mock(SkillCallback.class);
        module.performSkill(first, options(count -> { throw new IllegalStateException("failed"); }));
        module.performSkill(second, options(successful));

        module.update(1);

        verify(successful).execute(1);
        assertFalse(module.hasActiveAction(first));
        assertFalse(module.hasActiveAction(second));
    }

    @Test
    void failedCompletionDoesNotReleaseItsNewlyScheduledAction() {
        var module = module();
        var player = player(1);
        var next = mock(SkillCallback.class);
        module.performSkill(player, SkillPerformOptions.builder()
                .skillId(1)
                .swing(count -> {})
                .onComplete(count -> {
                    assertTrue(module.performSkill(player, options(next)));
                    throw new IllegalStateException("failed completion");
                })
                .build());

        module.update(1);

        assertTrue(module.hasActiveAction(player));
        verifyNoInteractions(next);
        module.update(1);
        verify(next).execute(1);
    }

    @Test
    void persistentPreferenceRepeatsSwingsAndCompletesOnceAfterTheLastOne() {
        var module = module();
        var player = player(1);
        var swing = mock(SkillCallback.class);
        var completion = mock(SkillCallback.class);
        mobiles.setProperty(player, WorldSkill.SWINGS_PROPERTY, 3L, false);
        module.performSkill(player, SkillPerformOptions.builder()
                .skillId(1).swing(swing).onComplete(completion).build());
        mobiles.setProperty(player, WorldSkill.SWINGS_PROPERTY, 9, false);

        module.update(0);
        verify(swing).execute(1);
        verifyNoInteractions(completion);
        module.update(0.5);
        verify(swing, never()).execute(2);
        assertTrue(module.hasActiveAction(player));
        verifyNoInteractions(completion);
        module.update(100);
        assertTrue(module.hasActiveAction(player));
        verifyNoInteractions(completion);
        module.update(1);
        assertFalse(module.hasActiveAction(player));
        module.update(100);

        var order = inOrder(swing, completion);
        order.verify(swing).execute(1);
        order.verify(swing).execute(2);
        order.verify(swing).execute(3);
        order.verify(completion).execute(3);
        verifyNoMoreInteractions(swing, completion);
    }

    @ParameterizedTest
    @ValueSource(doubles = {0, -1, 1.5, Double.NaN, Double.POSITIVE_INFINITY, 2147483648.0})
    void invalidPersistentSwingCountDefaultsToOne(double count) {
        var module = module();
        var player = player(1);
        var swing = mock(SkillCallback.class);
        mobiles.setProperty(player, WorldSkill.SWINGS_PROPERTY, count, false);
        module.performSkill(player, options(swing));

        module.update(1);
        module.update(1);

        verify(swing).execute(1);
        verifyNoMoreInteractions(swing);
        assertFalse(module.hasActiveAction(player));
    }

    @Test
    void runtimePreferenceDoesNotOverrideThePersistentDefault() {
        var module = module();
        var player = player(1);
        var swing = mock(SkillCallback.class);
        mobiles.setProperty(player, WorldSkill.SWINGS_PROPERTY, 5, true);
        module.performSkill(player, options(swing));

        module.update(1);

        verify(swing).execute(1);
        assertFalse(module.hasActiveAction(player));
    }

    @Test
    void reloadedPersistentPreferenceControlsTheAction() {
        var module = module();
        var player = player(1);
        mobiles.setProperty(player, WorldSkill.SWINGS_PROPERTY, 2, false);
        var reloaded = new UOPlayer(player.toData());
        var swing = mock(SkillCallback.class);
        module.performSkill(reloaded, options(swing));

        module.update(1);
        assertTrue(module.hasActiveAction(reloaded));
        module.update(1);

        verify(swing).execute(1);
        verify(swing).execute(2);
        assertFalse(module.hasActiveAction(reloaded));
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 5, 0, -3})
    void explicitSwingCountOverridesPersistenceWithoutChangingThePreference(int override) {
        var module = module();
        var player = player(1);
        var swing = mock(SkillCallback.class);
        var completion = mock(SkillCallback.class);
        mobiles.setProperty(player, WorldSkill.SWINGS_PROPERTY, 3, false);
        module.performSkill(player, SkillPerformOptions.builder()
                .skillId(1)
                .swingCount(override)
                .swing(swing)
                .onComplete(completion)
                .build());
        int expected = Math.max(1, override);
        for (int index = 1; index <= expected; index++) {
            module.update(1);
            verify(swing).execute(index);
        }
        module.update(1);

        verify(completion).execute(expected);
        verifyNoMoreInteractions(swing, completion);
        assertFalse(module.hasActiveAction(player));
        assertEquals(3, mobiles.getProperty(player, WorldSkill.SWINGS_PROPERTY, false));
    }

    @Test
    void nullSwingCountUsesThePersistentPreference() {
        var module = module();
        var player = player(1);
        var swing = mock(SkillCallback.class);
        mobiles.setProperty(player, WorldSkill.SWINGS_PROPERTY, 2, false);
        module.performSkill(player, SkillPerformOptions.builder()
                .skillId(1).swingCount(null).swing(swing).build());

        module.update(1);
        assertTrue(module.hasActiveAction(player));
        module.update(1);

        verify(swing).execute(1);
        verify(swing).execute(2);
        assertFalse(module.hasActiveAction(player));
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 0, 1, 3})
    void setSwingCountPersistsNormalizedPreferenceUsedByFutureActions(int count) {
        var module = module();
        var player = player(1);
        var swing = mock(SkillCallback.class);

        module.setSwingCount(player, count);

        int expected = Math.max(1, count);
        assertEquals(expected, mobiles.getProperty(player, WorldSkill.SWINGS_PROPERTY, false));
        var reloaded = new UOPlayer(player.toData());
        module.performSkill(reloaded, options(swing));
        for (int index = 1; index <= expected; index++) {
            module.update(1);
            verify(swing).execute(index);
        }
        assertFalse(module.hasActiveAction(reloaded));
        verifyNoMoreInteractions(swing);
    }

    @Test
    void setSwingCountRequiresPlayer() {
        assertThrows(NullPointerException.class, () -> module().setSwingCount(null, 3));
    }

    private UOPlayer player(int serial) {
        var data = new UOMobileData();
        data.setSerialId(serial);
        data.setPersistentAttrMap(new DefaultAttributeMap());
        return new UOPlayer(data);
    }

    private SkillModuleImpl module() {
        var module = new SkillModuleImpl(messages);
        var reader = mock(UOFileReader.class);
        when(reader.getSkill(1)).thenReturn(Optional.of(mock(Skill.class)));
        var registry = new DefaultFlowRegistry();
        registry.register("perform-skill", PerformSkillFlowDefinition.build(reader, module, messages, mobiles), PerformSkillContext.class);
        registry.register("update-skill-actions", UpdateSkillActionsFlowDefinition.build(module), UpdateSkillActionsContext.class);
        registry.register("set-skill-swing-count", SetSkillSwingCountFlowDefinition.build(mobiles), SetSkillSwingCountContext.class);
        module.initialize(new DefaultModuleContext(new DefaultFlowFacade(registry)));
        return module;
    }

    private SkillPerformOptions options(SkillCallback swing) {
        return SkillPerformOptions.builder().skillId(1).swing(swing).build();
    }
}
