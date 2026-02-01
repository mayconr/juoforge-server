package com.github.mayconr.juoserver.game.session.player;

import com.github.mayconr.juoserver.ServerProperties;
import com.github.mayconr.juoserver.common.event.EventBus;
import com.github.mayconr.juoserver.common.policy.PolicyService;
import com.github.mayconr.juoserver.common.useitem.ItemUseService;
import com.github.mayconr.juoserver.game.combat.CombatSystem;
import com.github.mayconr.juoserver.game.gameloop.GameLoop;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.session.SessionFanout;
import com.github.mayconr.juoserver.game.session.SessionOutbound;
import com.github.mayconr.juoserver.game.session.player.action.ActionService;
import com.github.mayconr.juoserver.game.session.player.click.ClickService;
import com.github.mayconr.juoserver.game.session.player.item.PlayerItemService;
import com.github.mayconr.juoserver.game.session.player.message.PlayerMessageService;
import com.github.mayconr.juoserver.game.session.player.movement.MovementService;
import com.github.mayconr.juoserver.game.session.player.skill.PlayerSkillService;
import com.github.mayconr.juoserver.game.session.player.speech.SpeechService;
import com.github.mayconr.juoserver.game.session.player.target.TargetService;
import com.github.mayconr.juoserver.game.session.player.vitals.PlayerVitalsTask;
import com.github.mayconr.juoserver.game.session.player.vitals.VitalsService;
import com.github.mayconr.juoserver.game.session.world.WorldInternal;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PlayerSessionFactory {

    private final EventBus eventBus;
    private final GameLoop gameLoop;
    private final CombatSystem combatSystem;
    private final PolicyService policyService;
    private final ServerProperties properties;
    private final ItemUseService itemUseService;
    private final RealmStorage storage;
    private WorldInternal worldInternal;

    public void initialize(WorldInternal worldInternal) {
        this.worldInternal = worldInternal;
    }

    public PlayerSession createPlayerSession(UOPlayer player, SessionOutbound outbound, SessionFanout fanout) {
        final var initializationService = new InitializationService(player, eventBus, worldInternal, outbound, fanout);
        final var speechService = new SpeechService(player, eventBus, fanout);
        final var movementService = new MovementService(player, eventBus, outbound, fanout, worldInternal);
        final var itemService = new PlayerItemService(player, fanout, outbound, storage, policyService);
        final var megaClilocService = new MegaClilocService(player, outbound, worldInternal);
        final var targetService = new TargetService(player, outbound);
        final var combatService = new CombatService(player, fanout, outbound, combatSystem);
        final var mountService = new MountService(player, outbound, fanout, worldInternal, policyService);
        final var clickService = new ClickService(player, worldInternal, outbound, mountService, itemService, itemUseService);
        final var vitalsService = new VitalsService(player, outbound, properties);
        final var skillService = new PlayerSkillService(player, outbound, eventBus);
        final var actionService = new ActionService(player, eventBus);
        final var statusService = new StatusService(player, outbound);
        final var messageService = new PlayerMessageService(outbound);

        final var session =
                new DefaultPlayerSession(
                        player,
                        properties,
                        storage,
                        initializationService,
                        policyService,
                        speechService,
                        movementService,
                        itemService,
                        clickService,
                        megaClilocService,
                        targetService,
                        combatService,
                        mountService,
                        vitalsService,
                        skillService,
                        actionService,
                        statusService,
                        messageService);
        gameLoop.addTask(new PlayerVitalsTask(session, vitalsService, properties));
        return session;
    }
}
