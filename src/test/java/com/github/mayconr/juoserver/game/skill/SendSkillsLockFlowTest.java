package com.github.mayconr.juoserver.game.skill;

import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.game.model.event.SkillLocked;
import com.github.mayconr.juoserver.game.skill.flow.lock.*;
import com.github.mayconr.juoserver.game.world.context.*;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SendSkillsLockFlowTest {
    @Test
    void updatesExistingAndMissingSkillsAndPublishesStoredValues() {
        var events = mock(EventBus.class);
        var player = mock(UOPlayer.class);
        var existing = SkillValue.of(1, 35, 100, SkillLock.UP);
        var skills = new SkillContainer(List.of(existing));
        when(player.getSkills()).thenReturn(skills);
        var registry = new DefaultFlowRegistry();
        registry.register("locks", SendSkillsLockFlowDefinition.build(events), SendSkillsLockContext.class);
        SkillModule module = new SkillModuleImpl(null, org.mockito.Mockito.mock(com.github.mayconr.juoserver.game.messaging.WorldMessage.class));
        module.initialize(new DefaultModuleContext(new DefaultFlowFacade(registry)));

        module.sendSkillsLock(player, List.of(SkillValue.of(1, SkillLock.DOWN), SkillValue.of(2, SkillLock.LOCKED)));

        assertSame(existing, skills.get(1));
        assertEquals(35d, skills.get(1).getBase());
        assertEquals(SkillLock.DOWN, skills.get(1).getLock());
        assertEquals(SkillLock.LOCKED, skills.get(2).getLock());
        verify(events).publish(new SkillLocked(player, List.of(skills.get(1), skills.get(2))));
    }
}