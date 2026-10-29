package com.github.mayconr.juoserver.game.skill;

import com.github.mayconr.juoserver.game.model.UOPlayer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PerformSkillTest {
    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.ValueSource(booleans = {true, false})
    void acceptsExistingSkillWithoutActionAndRejectsMissingSkill(boolean exists) {
        var reader = mock(com.github.mayconr.juoserver.infrastructure.datafile.UOFileReader.class);
        var skills = mock(SkillModule.class);
        var skill = mock(com.github.mayconr.juoforge.reader.skill.Skill.class);
        when(reader.getSkill(1)).thenReturn(exists ? java.util.Optional.of(skill) : java.util.Optional.empty());
        var context = new com.github.mayconr.juoserver.game.skill.flow.perform.PerformSkillContext(
                mock(UOPlayer.class), SkillActionOptions.builder().skillId(1).swing(count -> {}).build());
        var result = com.github.mayconr.juoserver.game.skill.flow.perform.PerformSkillFlowDefinition
                .build(reader, skills).execute(context);
        if (exists) {
            assertTrue(result.flowSucceeded());
            verify(skills).scheduleSkillAction(context.getPlayer(), context.getOptions());
        } else {
            assertEquals("SKILL_NOT_FOUND", result.code());
            verifyNoInteractions(skills);
        }
    }

    private SkillModuleImpl module() {
        var module = new SkillModuleImpl(null);
        var reader = mock(com.github.mayconr.juoserver.infrastructure.datafile.UOFileReader.class);
        var skill = mock(com.github.mayconr.juoforge.reader.skill.Skill.class);
        when(reader.getSkill(1)).thenReturn(java.util.Optional.of(skill));
        var registry = new com.github.mayconr.juoserver.game.world.context.DefaultFlowRegistry();
        registry.register("perform-skill",
                com.github.mayconr.juoserver.game.skill.flow.perform.PerformSkillFlowDefinition.build(reader, module),
                com.github.mayconr.juoserver.game.skill.flow.perform.PerformSkillContext.class);
        module.initialize(new com.github.mayconr.juoserver.game.world.context.DefaultModuleContext(
                new com.github.mayconr.juoserver.game.world.context.DefaultFlowFacade(registry)));
        return module;
    }
    @Test
    void singleSwingRunsOnNextUpdateAndCompletesExactlyOnce() {
        var module = module();
        var player = mock(UOPlayer.class);
        var swing = mock(SkillActionCallback.class);
        var completion = mock(SkillActionCallback.class);
        assertTrue(module.performSkill(player, SkillActionOptions.builder().skillId(1).swing(swing).onComplete(completion).build()));
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
    void singleSwingWithoutCompletionReleasesAction() {
        var module = module();
        var player = mock(UOPlayer.class);
        var swing = mock(SkillActionCallback.class);
        assertTrue(module.performSkill(player, SkillActionOptions.builder().skillId(1).swing(swing).build()));
        module.update(0);
        verify(swing).execute(1);
        assertFalse(module.hasActiveAction(player));
    }
}
