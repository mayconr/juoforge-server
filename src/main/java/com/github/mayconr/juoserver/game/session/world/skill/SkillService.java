package com.github.mayconr.juoserver.game.session.world.skill;

import com.github.mayconr.juoserver.game.event.EventBus;
import com.github.mayconr.juoserver.game.model.SkillValue;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.model.event.SkillGained;
import com.github.mayconr.juoserver.game.model.event.SkillGumpRequested;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SkillService {

    private final EventBus eventBus;
    private final RealmStorage storage;

    public void skillGained(UOMobile mobile, SkillValue skill) {
        eventBus.publish(new SkillGained(mobile, skill));
    }

    public void sendSkillGump(UOPlayer player, int requestedSkillSerialId) {
        final var mobile = storage.getMobileBySerialId(requestedSkillSerialId)
                .orElseThrow(()->new IllegalArgumentException("Mobile not found "+requestedSkillSerialId));
        eventBus.publish(new SkillGumpRequested(player, mobile));
    }
}
