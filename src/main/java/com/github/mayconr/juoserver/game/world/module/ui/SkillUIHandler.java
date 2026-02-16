package com.github.mayconr.juoserver.game.world.module.ui;

import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.model.event.SkillGumpRequested;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SkillUIHandler {

    private final EventBus  eventBus;
    private final RealmStorage storage;

    public void sendSkillGump(UOPlayer player, int requestedSkillSerialId) {
        final var mobile = storage.getMobileBySerialId(requestedSkillSerialId)
                .orElseThrow(()->new IllegalArgumentException("Mobile not found "+requestedSkillSerialId));
        eventBus.publish(new SkillGumpRequested(player, mobile));
    }

}
