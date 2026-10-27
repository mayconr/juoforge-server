package com.github.mayconr.juoserver.game.ui;

import com.github.mayconr.juoserver.game.model.UONpc;
import com.github.mayconr.juoserver.game.model.SendSkillType;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.model.event.SkillGumpRequested;
import com.github.mayconr.juoserver.game.ui.flow.skill.SkillGumpContext;
import com.github.mayconr.juoserver.game.ui.flow.skill.SkillGumpFlowDefinition;
import com.github.mayconr.juoserver.game.world.ModuleOperationException;
import com.github.mayconr.juoserver.game.world.context.*;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SkillGumpFlowTest {
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void requestsSkillsOfTheTargetOrReportsMissingMobile(boolean exists) {
        var storage = mock(RealmStorage.class);
        var events = mock(EventBus.class);
        var player = mock(UOPlayer.class);
        var target = mock(UONpc.class);
        when(storage.getMobile(42)).thenReturn(exists ? Optional.of(target) : Optional.empty());
        var registry = new DefaultFlowRegistry();
        registry.register("skills", SkillGumpFlowDefinition.build(events, storage), SkillGumpContext.class);
        UIModule ui = new UIModuleImpl(null, null, null);
        ui.initialize(new DefaultModuleContext(new DefaultFlowFacade(registry)));

        if (exists) {
            ui.sendSkillGump(player, 42, SendSkillType.FULL_LIST_WITH_CAP_AND_OPEN);
            verify(events).publish(new SkillGumpRequested(player, target, SendSkillType.FULL_LIST_WITH_CAP_AND_OPEN));
        } else {
            var error = assertThrows(ModuleOperationException.class, () -> ui.sendSkillGump(player, 42, SendSkillType.FULL_LIST_WITH_CAP_AND_OPEN));
            assertEquals("MOBILE_NOT_FOUND", error.getCode());
            verifyNoInteractions(events);
        }
        verify(storage).getMobile(42);
    }
}
