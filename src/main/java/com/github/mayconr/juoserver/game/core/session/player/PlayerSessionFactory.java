package com.github.mayconr.juoserver.game.core.session.player;

import com.github.mayconr.juoserver.game.core.combat.CombatSystem;
import com.github.mayconr.juoserver.game.core.database.Database;
import com.github.mayconr.juoserver.game.core.event.EventBus;
import com.github.mayconr.juoserver.game.core.gameloop.GameLoop;
import com.github.mayconr.juoserver.game.core.model.UOPlayer;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.group.ChannelGroup;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PlayerSessionFactory {

    private final ChannelGroup channelGroup;
    private final EventBus eventBus;
    private final Database database;
    private final GameLoop gameLoop;
    private final CombatSystem combatSystem;

    public PlayerSession createPlayerSession(UOPlayer player, ChannelHandlerContext ctx) {
        final var initializationServie =
                new InitializationService(player, eventBus, channelGroup, ctx, database);
        final var speechService = new SpeechService(player, eventBus, channelGroup);
        final var movementService =
                new MovementService(player, eventBus, channelGroup, ctx, database);
        final var itemIteractionService =
                new ItemInteractionService(player, channelGroup, ctx, database);

        final var megaClilocService = new MegaClilocService(player, ctx, database);
        final var targetService = new TargetService(player, ctx, eventBus);
        final var combatService = new CombatService(player, channelGroup, ctx, combatSystem);
        final var mountService = new MountService(player, ctx, channelGroup, database);
        final var clickService = new DoubleClickService(player, database, ctx, mountService);
        final var session =
                new DefaultPlayerSession(
                        player,
                        initializationServie,
                        speechService,
                        movementService,
                        itemIteractionService,
                        clickService,
                        megaClilocService,
                        targetService,
                        combatService,
                        mountService);
        gameLoop.addTask(new PlayerVitalsTask(session));
        return session;
    }
}
