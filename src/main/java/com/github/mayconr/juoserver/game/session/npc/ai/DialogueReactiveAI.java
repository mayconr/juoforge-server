package com.github.mayconr.juoserver.game.session.npc.ai;

import com.github.mayconr.juoserver.game.event.GameEvent;
import com.github.mayconr.juoserver.game.model.event.MobileSpeech;
import com.github.mayconr.juoserver.game.session.npc.CtxKeys;
import com.github.mayconr.juoserver.game.session.npc.NpcContext;
import com.github.mayconr.juoserver.game.session.npc.behavior.NpcBehavior;
import com.github.mayconr.juoserver.game.session.npc.profile.BehaviorProfile;

import java.util.List;

public class DialogueReactiveAI implements NpcAI {

    @Override
    public void onEvent(NpcContext ctx, GameEvent event) {
        switch (event) {
            case MobileSpeech spoke -> {
                ctx.set(CtxKeys.SPEAKER, spoke.player());
                ctx.set(CtxKeys.LAST_SPEECH, spoke.message());
            }
            default -> System.out.println("default");
        }
    }

    @Override
    public NpcBehavior decide(NpcContext ctx, BehaviorProfile profile) {
        final var npc = ctx.npc();
        final var supports = (List<String>) npc.getAttrMap().getOrDefault("behavior.supports", List.of());
        if (supports.contains(ctx.get(CtxKeys.LAST_SPEECH, String.class))) {
            return profile.service();
        }
        return profile.talk();
    }

}
