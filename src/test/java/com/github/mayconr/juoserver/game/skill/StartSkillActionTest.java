package com.github.mayconr.juoserver.game.skill;

import com.github.mayconr.juoserver.game.interaction.WorldInteraction;
import com.github.mayconr.juoserver.game.model.AnimationOptions;
import com.github.mayconr.juoserver.game.model.AnimationType;
import com.github.mayconr.juoserver.game.model.AnimationGroup;
import com.github.mayconr.juoserver.game.skill.flow.start.AnimateSkillActionStep;
import com.github.mayconr.juoserver.game.skill.flow.start.CalculateSkillActionDurationStep;
import com.github.mayconr.juoforge.reader.skill.Skill;
import com.github.mayconr.juoserver.game.messaging.WorldMessage;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.model.UOMobileData;
import com.github.mayconr.juoserver.game.model.DefaultAttributeMap;
import com.github.mayconr.juoserver.game.mobile.MobileModuleImpl;
import com.github.mayconr.juoserver.game.mobile.WorldMobile;
import com.github.mayconr.juoserver.game.model.event.message.MessageContent;
import com.github.mayconr.juoserver.game.skill.flow.start.StartSkillActionContext;
import com.github.mayconr.juoserver.game.skill.flow.update.UpdateSkillActionsContext;
import com.github.mayconr.juoserver.game.skill.flow.update.UpdateSkillActionsFlowDefinition;
import com.github.mayconr.juoserver.game.skill.flow.swings.SetSkillSwingCountContext;
import com.github.mayconr.juoserver.game.skill.flow.swings.SetSkillSwingCountFlowDefinition;
import com.github.mayconr.juoserver.game.skill.flow.start.StartSkillActionFlowDefinition;
import com.github.mayconr.juoserver.game.world.ModuleOperationException;
import com.github.mayconr.juoserver.game.world.context.DefaultFlowFacade;
import com.github.mayconr.juoserver.game.world.context.DefaultFlowRegistry;
import com.github.mayconr.juoserver.game.world.context.DefaultModuleContext;
import com.github.mayconr.juoserver.infrastructure.datafile.UOFileReader;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;

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

class StartSkillActionTest {
    private final WorldMessage messages = mock(WorldMessage.class);
    private final WorldInteraction interaction = mock(WorldInteraction.class);
    private final WorldMobile mobiles = new MobileModuleImpl(null, null, null, null, null);

    @Test
    void lowerDexterityLengthensAnimationFramesAndSwingDelay() {
        var reader = mock(UOFileReader.class);
        var skills = mock(SkillModule.class);
        var player = mock(UOPlayer.class);
        when(player.getDexterity()).thenReturn(50);
        when(reader.getSkill(1)).thenReturn(Optional.of(mock(Skill.class)));
        when(skills.reserveSkillAction(eq(player), any())).thenReturn(true);
        var options = SkillActionOptions.builder()
                .skillId(1)
                .swingCount(2)
                .animationGroup(AnimationGroup.ATTACK_ONE_HANDED_WIDE)
                .onSwing(count -> {})
                .build();
        var context = new StartSkillActionContext(player, options);

        var result = StartSkillActionFlowDefinition.build(reader, skills, messages, mobiles, interaction)
                .execute(context);

        assertTrue(result.flowSucceeded());
        assertEquals(200.0 / 150.0, context.getDurationSeconds(), 1e-9);
        verify(interaction).sendAnimation(player,
                AnimationOptions.simpleForward(AnimationType.ATTACK_ONE_HANDED_WIDE, 27));
        var action = ArgumentCaptor.forClass(SkillAction.class);
        verify(skills).reserveSkillAction(eq(player), action.capture());
        assertFalse(action.getValue().advance(1.33));
        assertTrue(action.getValue().advance(0.01));
        action.getValue().executeSwing();
        assertFalse(action.getValue().advance(1.33));
        assertTrue(action.getValue().advance(0.01));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void sendsMiningAnimationBeforeScheduling(boolean mounted) {
        var reader = mock(UOFileReader.class);
        var skills = mock(SkillModule.class);
        var player = mock(UOPlayer.class);
        when(player.isMounted()).thenReturn(mounted);
        when(player.getDexterity()).thenReturn(100);
        when(reader.getSkill(1)).thenReturn(Optional.of(mock(Skill.class)));
        when(skills.reserveSkillAction(eq(player), any())).thenReturn(true);
        var options = SkillActionOptions.builder()
                .skillId(1)
                .swingCount(1)
                .animationGroup(AnimationGroup.ATTACK_ONE_HANDED_WIDE)
                .onSwing(count -> {})
                .build();

        StartSkillActionFlowDefinition.build(reader, skills, messages, mobiles, interaction)
                .execute(new StartSkillActionContext(player, options));

        var type = mounted ? AnimationType.MOUNTED_ATTACK_MELEE : AnimationType.ATTACK_ONE_HANDED_WIDE;
        var order = inOrder(interaction, skills);
        order.verify(interaction).sendAnimation(player, AnimationOptions.simpleForward(type, 20));
        order.verify(skills).reserveSkillAction(eq(player), any());
    }

    @Test
    void preservesCustomAnimationWhenMounted() {
        var player = mock(UOPlayer.class);
        when(player.isMounted()).thenReturn(true);
        when(player.getDexterity()).thenReturn(100);
        var options = SkillActionOptions.builder().animationGroup(AnimationGroup.SALUTE).build();

        var context = new StartSkillActionContext(player, options);
        new CalculateSkillActionDurationStep().execute(context);
        new AnimateSkillActionStep(interaction).execute(context);

        verify(interaction).sendAnimation(player, AnimationOptions.simpleForward(AnimationType.SALUTE, 20));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void acceptsExistingSkillWithoutActionAndRejectsMissingSkill(boolean exists) {
        var reader = mock(UOFileReader.class);
        var skills = mock(SkillModule.class);
        when(reader.getSkill(1)).thenReturn(exists ? Optional.of(mock(Skill.class)) : Optional.empty());
        var player = player(1);
        when(skills.reserveSkillAction(eq(player), any())).thenReturn(true);
        var context = new StartSkillActionContext(player, options(count -> {}));

        var result = StartSkillActionFlowDefinition.build(reader, skills, messages, mobiles, interaction).execute(context);

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
    void everySwingCycleAnimatesThenWaitsTheDurationBeforeSwinging() {
        var module = module();
        var player = player(1);
        var swing = mock(SkillCallback.class);
        var completion = mock(SkillCallback.class);
        var animation = AnimationOptions.simpleForward(AnimationType.ATTACK_ONE_HANDED_WIDE, 20);
        module.startSkillAction(player, animatedOptions(3, swing, completion));

        verify(interaction).sendAnimation(player, animation);
        module.update(0.5);
        verifyNoInteractions(swing);
        for (int index = 0; index < 3; index++) {
            module.update(index == 0 ? 0.5 : 1);
        }
        module.update(100);

        var order = inOrder(interaction, swing, completion);
        order.verify(interaction).sendAnimation(player, animation);
        order.verify(swing).execute(1);
        order.verify(interaction).sendAnimation(player, animation);
        order.verify(swing).execute(2);
        order.verify(interaction).sendAnimation(player, animation);
        order.verify(swing).execute(3);
        order.verify(completion).execute(3);
        verifyNoMoreInteractions(interaction, swing, completion);
    }

    @Test
    void cancelledActionDoesNotAnimateAnotherCycle() {
        var module = module();
        var player = player(1);
        module.startSkillAction(player, animatedOptions(3, count -> {}, count -> {}));

        player.setLocation(1, 0);
        module.update(1);

        verify(interaction).sendAnimation(eq(player), any());
        verifyNoMoreInteractions(interaction);
    }

    @Test
    void failedSwingDoesNotAnimateAnotherCycle() {
        var module = module();
        var player = player(1);
        module.startSkillAction(player, animatedOptions(3, count -> {
            throw new IllegalStateException("failed");
        }, count -> {}));

        module.update(1);

        assertFalse(module.hasActiveAction(player));
        verify(interaction).sendAnimation(eq(player), any());
        verifyNoMoreInteractions(interaction);
    }

    @Test
    void singleSwingWaitsTheDurationAndCompletesExactlyOnce() {
        var module = module();
        var player = player(1);
        var swing = mock(SkillCallback.class);
        var completion = mock(SkillCallback.class);
        assertTrue(module.startSkillAction(player, SkillActionOptions.builder()
                .skillId(1).onSwing(swing).onComplete(completion).build()));
        assertTrue(module.hasActiveAction(player));
        verifyNoInteractions(swing, completion);

        module.update(0.5);
        assertTrue(module.hasActiveAction(player));
        verifyNoInteractions(swing, completion);
        module.update(0.5);
        assertFalse(module.hasActiveAction(player));
        module.update(1);

        var order = inOrder(swing, completion);
        order.verify(swing).execute(1);
        order.verify(completion).execute(1);
        verifyNoMoreInteractions(swing, completion);
    }

    @Test
    void everySwingWaitsTheDurationIncludingTheFirst() {
        var module = module();
        var player = player(1);
        var swing = mock(SkillCallback.class);
        assertTrue(module.startSkillAction(player, SkillActionOptions.builder()
                .skillId(1).swingCount(2).onSwing(swing).build()));

        module.update(0.5);
        verifyNoInteractions(swing);
        module.update(0.5);
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
        module.startSkillAction(player, SkillActionOptions.builder()
                .skillId(1).swingCount(3).onSwing(swing).onComplete(completion).build());

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
        module.startSkillAction(player, SkillActionOptions.builder()
                .skillId(1).swingCount(3).onSwing(swing).onComplete(completion).build());

        module.update(1);
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
        module.startSkillAction(player, SkillActionOptions.builder()
                .skillId(1).swingCount(2).onSwing(swing).onComplete(completion).build());

        module.update(1);
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
        module.startSkillAction(player, SkillActionOptions.builder()
                .skillId(1).swingCount(2).onSwing(swing).build());

        module.update(1);
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
        module.startSkillAction(player, SkillActionOptions.builder()
                .skillId(1)
                .swingCount(2)
                .onSwing(swing)
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
        assertTrue(module.startSkillAction(player, options(original)));
        assertFalse(module.startSkillAction(player, options(rejected)));

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
        module.startSkillAction(player, SkillActionOptions.builder()
                .skillId(1).onSwing(swing).onComplete(completion).build());

        module.update(1);

        assertFalse(module.hasActiveAction(player));
        verifyNoInteractions(completion);
    }

    @Test
    void completionCanScheduleAnotherActionForTheNextUpdate() {
        var module = module();
        var player = player(1);
        var next = mock(SkillCallback.class);
        module.startSkillAction(player, SkillActionOptions.builder()
                .skillId(1)
                .onSwing(count -> {})
                .onComplete(count -> assertTrue(module.startSkillAction(player, options(next))))
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
        module.startSkillAction(player, SkillActionOptions.builder()
                .skillId(1).onSwing(swing).build());

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
        module.startSkillAction(first, options(count -> { throw new IllegalStateException("failed"); }));
        module.startSkillAction(second, options(successful));

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
        module.startSkillAction(player, SkillActionOptions.builder()
                .skillId(1)
                .onSwing(count -> {})
                .onComplete(count -> {
                    assertTrue(module.startSkillAction(player, options(next)));
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
        module.startSkillAction(player, SkillActionOptions.builder()
                .skillId(1).onSwing(swing).onComplete(completion).build());
        mobiles.setProperty(player, WorldSkill.SWINGS_PROPERTY, 9, false);

        module.update(1);
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
        module.startSkillAction(player, options(swing));

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
        module.startSkillAction(player, options(swing));

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
        module.startSkillAction(reloaded, options(swing));

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
        module.startSkillAction(player, SkillActionOptions.builder()
                .skillId(1)
                .swingCount(override)
                .onSwing(swing)
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
        module.startSkillAction(player, SkillActionOptions.builder()
                .skillId(1).swingCount(null).onSwing(swing).build());

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
        module.startSkillAction(reloaded, options(swing));
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
        data.setDexterity(100);
        return new UOPlayer(data);
    }

    private SkillModuleImpl module() {
        var module = new SkillModuleImpl(messages);
        var reader = mock(UOFileReader.class);
        when(reader.getSkill(1)).thenReturn(Optional.of(mock(Skill.class)));
        var registry = new DefaultFlowRegistry();
        registry.register("start-skill-action", StartSkillActionFlowDefinition.build(reader, module, messages, mobiles, interaction), StartSkillActionContext.class);
        registry.register("update-skill-actions", UpdateSkillActionsFlowDefinition.build(module, interaction), UpdateSkillActionsContext.class);
        registry.register("set-skill-swing-count", SetSkillSwingCountFlowDefinition.build(mobiles), SetSkillSwingCountContext.class);
        module.initialize(new DefaultModuleContext(new DefaultFlowFacade(registry)));
        return module;
    }

    private SkillActionOptions animatedOptions(int swings, SkillCallback swing, SkillCallback completion) {
        return SkillActionOptions.builder()
                .skillId(1)
                .swingCount(swings)
                .animationGroup(AnimationGroup.ATTACK_ONE_HANDED_WIDE)
                .onSwing(swing)
                .onComplete(completion)
                .build();
    }

    private SkillActionOptions options(SkillCallback swing) {
        return SkillActionOptions.builder().skillId(1).onSwing(swing).build();
    }
}
