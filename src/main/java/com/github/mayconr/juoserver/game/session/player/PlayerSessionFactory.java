package com.github.mayconr.juoserver.game.session.player;

import com.github.mayconr.juoserver.common.event.EventBus;
import com.github.mayconr.juoserver.common.policy.ActionPolicyService;
import com.github.mayconr.juoserver.game.combat.CombatSystem;
import com.github.mayconr.juoserver.game.gameloop.GameLoop;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.session.SessionFanout;
import com.github.mayconr.juoserver.game.session.SessionOutbound;
import com.github.mayconr.juoserver.game.session.player.speech.SpeechService;
import com.github.mayconr.juoserver.game.session.player.target.TargetService;
import com.github.mayconr.juoserver.game.session.world.WorldSession;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PlayerSessionFactory {

    private final EventBus eventBus;
    private final GameLoop gameLoop;
    private final CombatSystem combatSystem;
    private final ActionPolicyService policyService;
    private WorldSession worldSession;

    public void initialize(WorldSession worldSession) {
        this.worldSession = worldSession;
    }

    public PlayerSession createPlayerSession(UOPlayer player, SessionOutbound outbound, SessionFanout fanout) {
        final var initializationService = new InitializationService(player, eventBus, worldSession, outbound, fanout);
        final var speechService = new SpeechService(player, eventBus, fanout);
        final var movementService = new MovementService(player, eventBus, outbound, fanout, worldSession);
        final var itemIterationService = new ItemInteractionService(player, fanout, outbound, worldSession);
        final var megaClilocService = new MegaClilocService(player, outbound, worldSession);
        final var targetService = new TargetService(player, outbound);
        final var combatService = new CombatService(player, fanout, outbound, combatSystem);
        final var mountService = new MountService(player, outbound, fanout, worldSession);
        final var clickService = new DoubleClickService(player, worldSession, outbound, mountService);
        final var session =
                new DefaultPlayerSession(
                        player,
                        initializationService,
                        policyService,
                        speechService,
                        movementService,
                        itemIterationService,
                        clickService,
                        megaClilocService,
                        targetService,
                        combatService,
                        mountService);
        gameLoop.addTask(new PlayerVitalsTask(session));
        return session;
    }
}
