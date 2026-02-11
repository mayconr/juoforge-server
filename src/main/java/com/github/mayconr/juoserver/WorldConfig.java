package com.github.mayconr.juoserver;

import com.github.mayconr.juoserver.game.ai.ollama.OllamaClientChatImpl;
import com.github.mayconr.juoserver.game.ai.ollama.OllanaClient;
import com.github.mayconr.juoserver.game.combat.CombatSystem;
import com.github.mayconr.juoserver.game.combat.DefaultCombatSystem;
import com.github.mayconr.juoserver.game.event.DefaultEventBus;
import com.github.mayconr.juoserver.game.event.EventBus;
import com.github.mayconr.juoserver.game.gameloop.DefaultGameLoop;
import com.github.mayconr.juoserver.game.gameloop.GameLoop;
import com.github.mayconr.juoserver.game.gump.DefaultGumpSystem;
import com.github.mayconr.juoserver.game.gump.GumpSystem;
import com.github.mayconr.juoserver.game.model.event.MobileMoved;
import com.github.mayconr.juoserver.game.npc.NpcSessionFactory;
import com.github.mayconr.juoserver.game.player.DefaultSessionManager;
import com.github.mayconr.juoserver.game.player.PlayerSessionFactory;
import com.github.mayconr.juoserver.game.player.SessionFanout;
import com.github.mayconr.juoserver.game.player.SessionManager;
import com.github.mayconr.juoserver.game.policy.PolicyRegistry;
import com.github.mayconr.juoserver.game.policy.PolicyService;
import com.github.mayconr.juoserver.game.reader.UOFileReader;
import com.github.mayconr.juoserver.game.rng.DefaultRNG;
import com.github.mayconr.juoserver.game.rng.RNG;
import com.github.mayconr.juoserver.game.skill.DefaultSkillSystem;
import com.github.mayconr.juoserver.game.skill.SkillSystem;
import com.github.mayconr.juoserver.game.template.*;
import com.github.mayconr.juoserver.game.trigger.item.ItemUseRegistry;
import com.github.mayconr.juoserver.game.trigger.item.ItemUseService;
import com.github.mayconr.juoserver.game.world.DefaultWorld;
import com.github.mayconr.juoserver.game.world.SerialGenerator;
import com.github.mayconr.juoserver.game.world.WorldInternal;
import com.github.mayconr.juoserver.game.world.action.ActionService;
import com.github.mayconr.juoserver.game.world.animation.AnimationService;
import com.github.mayconr.juoserver.game.world.click.DoubleClickService;
import com.github.mayconr.juoserver.game.world.click.SingleClickService;
import com.github.mayconr.juoserver.game.world.combat.CombatService;
import com.github.mayconr.juoserver.game.world.item.ItemCreationService;
import com.github.mayconr.juoserver.game.world.item.ItemDropService;
import com.github.mayconr.juoserver.game.world.item.ItemEquipService;
import com.github.mayconr.juoserver.game.world.message.MessageService;
import com.github.mayconr.juoserver.game.world.mount.MountService;
import com.github.mayconr.juoserver.game.world.movement.MovementService;
import com.github.mayconr.juoserver.game.world.movement.RangeDetection;
import com.github.mayconr.juoserver.game.world.npc.NpcService;
import com.github.mayconr.juoserver.game.world.player.PlayerCreationService;
import com.github.mayconr.juoserver.game.world.player.PlayerLoginService;
import com.github.mayconr.juoserver.game.world.player.PlayerRemovalService;
import com.github.mayconr.juoserver.game.world.skill.SkillService;
import com.github.mayconr.juoserver.game.world.speech.SpeechService;
import com.github.mayconr.juoserver.game.world.status.StatusService;
import com.github.mayconr.juoserver.game.world.target.TargetService;
import com.github.mayconr.juoserver.game.world.tooltip.TooltipService;
import com.github.mayconr.juoserver.game.world.vendor.VendorService;
import com.github.mayconr.juoserver.game.world.vitals.VitalsService;
import com.github.mayconr.juoserver.infrastructure.storage.CachedRealmStorage;
import com.github.mayconr.juoserver.infrastructure.storage.ItemStorage;
import com.github.mayconr.juoserver.infrastructure.storage.MobileStorage;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;
import io.netty.channel.group.ChannelGroup;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.nio.file.Path;

@EnableConfigurationProperties(ServerProperties.class)
public class WorldConfig {

    // =========================================================================
    // Core Infrastructure
    // =========================================================================

    @Bean
    public EventBus eventBus() {
        return new DefaultEventBus();
    }

    @Bean
    public GameLoop gameLoop(ServerProperties properties) {
        final var gameLoop = new DefaultGameLoop(properties);
        Runtime.getRuntime().addShutdownHook(new Thread(gameLoop::stop));
        return gameLoop.start();
    }

    @Bean
    public OllanaClient ollanaClient() {
        return new OllamaClientChatImpl("http://localhost:11434");
    }

    @Bean
    public RNG rng() {
        return new DefaultRNG();
    }

    // =========================================================================
    // Policy
    // =========================================================================

    @Bean
    public PolicyRegistry policyRegistry() {
        return new PolicyRegistry();
    }

    @Bean
    public PolicyService policyService(PolicyRegistry registry) {
        return new PolicyService(registry);
    }

    // =========================================================================
    // Item Use
    // =========================================================================

    @Bean
    public ItemUseRegistry itemUseRegistry() {
        return new ItemUseRegistry();
    }

    @Bean
    public ItemUseService itemUseService(ItemUseRegistry registry) {
        return new ItemUseService(registry);
    }

    // =========================================================================
    // Combat / Systems
    // =========================================================================

    @Bean
    public CombatSystem combatSystem(GameLoop gameLoop, ChannelGroup channelGroup) {
        final var combatSystem = new DefaultCombatSystem(channelGroup);
        gameLoop.addTask(combatSystem);
        return combatSystem;
    }

    @Bean
    public GumpSystem gumpSystem(EventBus eventBus) {
        return new DefaultGumpSystem(eventBus);
    }

    @Bean
    public SkillSystem skillSystem(ServerProperties properties, RNG rng) {
        return new DefaultSkillSystem(properties, rng);
    }

    // =========================================================================
    // AI
    // =========================================================================

    @Bean
    public NpcSessionFactory npcSessionFactory(
            EventBus eventBus,
            SessionFanout fanout) {

        return new NpcSessionFactory(eventBus, fanout);
    }

    // =========================================================================
    // Templates
    // =========================================================================

    @Bean
    public TemplateLoader<MountTemplate> mountTemplateLoader() {
        return new JsonTemplateLoader<>(Path.of("template/mounts"), MountTemplate.class);
    }

    @Bean
    public TemplateLoader<NpcTemplate> npcTemplateLoader() {
        return new JsonTemplateLoader<>(Path.of("template/npcs"), NpcTemplate.class);
    }

    @Bean
    public NpcTemplateRegistry npcTemplateRegistry(TemplateLoader<NpcTemplate> templateLoader) {
        return new DefaultNpcTemplateRegistry(templateLoader.load());
    }

    @Bean
    public TemplateLoader<ItemTemplate> itemTemplateLoader() {
        return new JsonTemplateLoader<>(Path.of("template/items"), ItemTemplate.class);
    }

    @Bean
    public ItemTemplateRegistry itemTemplateRegistry(TemplateLoader<ItemTemplate> itemTemplateLoader) {
        return new DefaultItemTemplateRegistry(itemTemplateLoader.load());
    }

    // =========================================================================
    // Storage
    // =========================================================================

    @Bean
    public RealmStorage realmStorage(
            MobileStorage mobileStorage,
            ItemStorage itemStorage) {

        return new CachedRealmStorage(mobileStorage, itemStorage);
    }

    // =========================================================================
    // Sessions / World
    // =========================================================================

    @Bean
    public SessionManager sessionRegistry(WorldInternal world, EventBus eventBus) {
        return new DefaultSessionManager(world, eventBus);
    }

    @Bean
    public PlayerSessionFactory playerSessionFactory(ServerProperties properties) {
        return new PlayerSessionFactory(properties);
    }

    @Bean
    public WorldInternal worldSession(
            RealmStorage storage,
            EventBus eventBus,
            ItemTemplateRegistry itemTemplateRegistry,
            GameLoop gameLoop,
            SkillSystem skillSystem,
            ServerProperties properties,
            PolicyService policyService,
            NpcTemplateRegistry npcTemplateRegistry,
            NpcSessionFactory npcSessionFactory,
            ItemUseService itemUseService,
            CombatSystem combatSystem,
            GumpSystem gumpSystem) {

        final var serialGenerator = new SerialGenerator(storage);
        final var messageService = new MessageService(eventBus);
        final var itemService = new ItemCreationService(
                serialGenerator,
                itemTemplateRegistry,
                storage,
                eventBus
        );
        final var playerMobileService = new PlayerCreationService(
                serialGenerator,
                storage,
                itemTemplateRegistry,
                properties,
                policyService
        );
        final var fileReader = new UOFileReader(properties);
        final var animationService = new AnimationService(eventBus);
        final var npcService = new NpcService(
                npcSessionFactory,
                npcTemplateRegistry,
                itemTemplateRegistry,
                serialGenerator,
                storage,
                eventBus);
        final var movementService = new MovementService(eventBus, storage);
        final var speechService = new SpeechService(eventBus);
        final var equipItemService = new ItemEquipService(storage, eventBus);
        final var playerRemovalService = new PlayerRemovalService(storage, eventBus);
        final var skillService = new SkillService(eventBus, storage);
        final var statusService = new StatusService(eventBus, storage);
        final var tooltipService = new TooltipService(eventBus, storage);
        final var itemDropService = new ItemDropService(eventBus, storage, policyService);
        final var loginService = new PlayerLoginService(eventBus, storage);
        final var targetService = new TargetService(eventBus);
        final var doubleClickService = new DoubleClickService(eventBus, storage, itemUseService, policyService);
        final var singleClick = new SingleClickService(storage);
        final var combatService = new CombatService(eventBus, combatSystem, storage);
        final var merchantService = new VendorService(eventBus);
        final var actionService = new ActionService(eventBus);
        final var mountService = new MountService(eventBus, storage, policyService, itemTemplateRegistry, serialGenerator);
        final var vitalsService = new VitalsService(eventBus, properties);

        final var world = new DefaultWorld(
                serialGenerator,
                storage,
                gameLoop,
                skillSystem,
                gumpSystem,
                messageService,
                itemService,
                playerMobileService,
                fileReader,
                animationService,
                npcService,
                movementService,
                speechService,
                equipItemService,
                playerRemovalService,
                skillService,
                statusService,
                tooltipService,
                itemDropService,
                loginService,
                targetService,
                doubleClickService,
                singleClick,
                combatService,
                merchantService,
                actionService,
                mountService,
                vitalsService
        );

        eventBus.register(MobileMoved.class, new RangeDetection(world, eventBus, properties));
        world.initialize();
        return world;
    }
}
