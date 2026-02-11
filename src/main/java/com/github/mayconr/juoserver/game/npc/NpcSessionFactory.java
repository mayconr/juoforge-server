package com.github.mayconr.juoserver.game.npc;

import com.github.mayconr.juoserver.game.event.EventBus;
import com.github.mayconr.juoserver.game.gameloop.GameTask;
import com.github.mayconr.juoserver.game.model.UONpc;
import com.github.mayconr.juoserver.game.player.SessionFanout;
import com.github.mayconr.juoserver.game.npc.ai.NpcAiFactory;
import com.github.mayconr.juoserver.game.npc.profile.BehaviorProfileFactory;
import com.github.mayconr.juoserver.game.world.WorldInternal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class NpcSessionFactory {

    private final EventBus eventBus;
    private final SessionFanout fanout;

    public NpcSession create(UONpc npc, WorldInternal world) {
        validate(npc);

        final var profileFactory = new BehaviorProfileFactory();
        final var aiFactory = new NpcAiFactory();

        try {
            final var profile = profileFactory.create(npc.getBehavior().profile());
            final var ai = aiFactory.create(npc.getBehavior().ai());

            final var session = new DefaultNpcSession(npc, fanout, eventBus, profile, ai);

            session.initialize(world);

            world.scheduleTask(new ThinkTask(session));

            log.info("Session created for NPC [{}-{}]", npc.getSerialId(), npc.getName());
            return session;
        } catch (Exception e) {
            throw new IllegalStateException("Unable to create session for NPC ["+npc.getId()+"-"+npc.getName()+"]", e);
        }
    }

    private void validate(UONpc npc) {
        if (npc.getBehavior() == null) {
            throw new IllegalArgumentException("Behavior not found for npc ["+npc.getSerialId()+"-"+npc.getName()+"]");
        }
    }

    private record ThinkTask(NpcSession session) implements GameTask {

        @Override
        public void execute(long currentTick, double delta) {
            session.think(delta);
        }

        @Override
        public boolean isDone() {
            return false;
        }
    }
}
