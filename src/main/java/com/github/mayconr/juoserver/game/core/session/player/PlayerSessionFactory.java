package com.github.mayconr.juoserver.game.core.session.player;

import com.github.mayconr.juoserver.game.core.combat.CombatSystem;
import com.github.mayconr.juoserver.game.core.event.EventBus;
import com.github.mayconr.juoserver.game.core.gameloop.GameLoop;
import com.github.mayconr.juoserver.game.core.model.UOPlayer;
import com.github.mayconr.juoserver.game.core.session.SessionFanout;
import com.github.mayconr.juoserver.game.core.session.SessionOutbound;
import com.github.mayconr.juoserver.game.core.session.player.speech.SpeechService;
import com.github.mayconr.juoserver.game.storage.WorldService;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.group.ChannelGroup;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PlayerSessionFactory {

    private final ChannelGroup channelGroup;
    private final EventBus eventBus;
    private final WorldService worldService;
    private final GameLoop gameLoop;
    private final CombatSystem combatSystem;

    public PlayerSession createPlayerSession(UOPlayer player, ChannelHandlerContext ctx, SessionOutbound outbound, SessionFanout fanout) {
        final var initializationService = new InitializationService(player, eventBus, worldService, outbound, fanout);
        final var speechService = new SpeechService(player, eventBus, fanout);
        final var movementService = new MovementService(player, eventBus, outbound, fanout, worldService);
        final var itemIterationService = new ItemInteractionService(player, channelGroup, ctx, worldService);
        final var megaClilocService = new MegaClilocService(player, outbound, worldService);
        final var targetService = new TargetService(player, outbound, eventBus);
        final var combatService = new CombatService(player, channelGroup, ctx, combatSystem);
        final var mountService = new MountService(player, outbound, fanout, worldService);
        final var clickService = new DoubleClickService(player, worldService, outbound, mountService);
        final var session =
                new DefaultPlayerSession(
                        player,
                        initializationService,
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
