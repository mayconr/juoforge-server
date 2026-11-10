package com.github.mayconr.juoserver.game.skill;

import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.model.UOMobileData;
import com.github.mayconr.juoserver.game.model.event.SkillUpdated;
import com.github.mayconr.juoserver.game.skill.flow.set.SetSkillContext;
import com.github.mayconr.juoserver.game.skill.flow.set.SetSkillFlowDefinition;
import com.github.mayconr.juoserver.game.world.ModuleOperationException;
import com.github.mayconr.juoserver.game.world.context.DefaultFlowFacade;
import com.github.mayconr.juoserver.game.world.context.DefaultFlowRegistry;
import com.github.mayconr.juoserver.game.world.context.DefaultModuleContext;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class SetSkillFlowTest {
    private final EventBus events = mock(EventBus.class);

    @ParameterizedTest
    @ValueSource(ints = {-1, 65536})
    void flowRejectsInvalidIdsWithoutMutationOrNotification(int skillId) {
        var player = new UOPlayer(new UOMobileData());

        var result = SetSkillFlowDefinition.build(events).execute(new SetSkillContext(player, skillId, 50));

        assertEquals("INVALID_SKILL_ID", result.code());
        assertTrue(player.getSkills().getSkillValues().isEmpty());
        verifyNoInteractions(events);
    }

    @ParameterizedTest
    @ValueSource(doubles = {Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY})
    void flowRejectsNonfiniteValuesWithoutMutationOrNotification(double value) {
        var player = new UOPlayer(new UOMobileData());

        var result = SetSkillFlowDefinition.build(events).execute(new SetSkillContext(player, 1, value));

        assertEquals("INVALID_SKILL_VALUE", result.code());
        assertTrue(player.getSkills().getSkillValues().isEmpty());
        verifyNoInteractions(events);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 65535})
    void acceptsBoundaryIdsAndPreservesClamping(int skillId) {
        var player = new UOPlayer(new UOMobileData());

        var result = SetSkillFlowDefinition.build(events).execute(new SetSkillContext(player, skillId, 150));

        assertTrue(result.flowSucceeded());
        assertEquals(100, player.getSkills().get(skillId).getBase());
        verify(events).publish(any(SkillUpdated.class));
    }

    @Test
    void moduleDelegatesValidationAndKeepsNullCheck() {
        var module = new SkillModuleImpl(null);
        var registry = new DefaultFlowRegistry();
        registry.register("set-skill", SetSkillFlowDefinition.build(events), SetSkillContext.class);
        module.initialize(new DefaultModuleContext(new DefaultFlowFacade(registry)));
        var player = new UOPlayer(new UOMobileData());

        var invalidId = assertThrows(ModuleOperationException.class, () -> module.setSkill(player, -1, 50));
        assertEquals("INVALID_SKILL_ID", invalidId.getCode());
        var invalidValue = assertThrows(ModuleOperationException.class, () -> module.setSkill(player, 1, Double.NaN));
        assertEquals("INVALID_SKILL_VALUE", invalidValue.getCode());
        assertThrows(NullPointerException.class, () -> module.setSkill(null, 1, 50));
        verifyNoInteractions(events);
    }
}
