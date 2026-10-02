package com.github.mayconr.juoserver.game.world;

import com.github.mayconr.juoserver.game.map.WorldMap;
import com.github.mayconr.juoserver.game.storage.WorldStorage;
import com.github.mayconr.juoserver.game.storage.StorageModule;
import com.github.mayconr.juoserver.game.storage.StorageModuleImpl;
import com.github.mayconr.juoserver.game.random.WorldRandom;
import com.github.mayconr.juoserver.game.random.RandomModule;
import com.github.mayconr.juoserver.game.random.RandomModuleImpl;
import com.github.mayconr.juoserver.game.devtools.WorldDevTools;
import com.github.mayconr.juoserver.game.devtools.DevToolsModule;
import com.github.mayconr.juoserver.game.devtools.DevToolsModuleImpl;
import com.github.mayconr.juoserver.game.map.MapModule;
import com.github.mayconr.juoserver.game.messaging.WorldMessage;
import com.github.mayconr.juoserver.game.npc.WorldNpc;
import com.github.mayconr.juoserver.game.spell.WorldSpell;
import com.github.mayconr.juoserver.game.damage.WorldDamage;
import com.github.mayconr.juoserver.WorldCfg;
import com.github.mayconr.juoserver.game.GamePlaySettings;
import com.github.mayconr.juoserver.game.spell.SpellModule;
import com.github.mayconr.juoserver.game.spell.trigger.SpellCastRegistry;
import com.github.mayconr.juoserver.game.spell.SpellModuleImpl;
import com.github.mayconr.juoserver.game.spell.template.SpellTemplate;
import com.github.mayconr.juoserver.game.ai.AIEngineImpl;
import com.github.mayconr.juoserver.game.ai.AIModule;
import com.github.mayconr.juoserver.game.ai.WorldAI;
import com.github.mayconr.juoserver.game.ai.AIModuleImpl;
import com.github.mayconr.juoserver.game.ai.actions.SellListAction;
import com.github.mayconr.juoserver.game.ai.actions.SpeechAction;
import com.github.mayconr.juoserver.game.ai.actions.WalkAction;
import com.github.mayconr.juoserver.game.combat.CombatHandler;
import com.github.mayconr.juoserver.game.combat.CombatModule;
import com.github.mayconr.juoserver.game.combat.WorldCombat;
import com.github.mayconr.juoserver.game.combat.CombatModuleImpl;
import com.github.mayconr.juoserver.game.combat.VitalsHandler;
import com.github.mayconr.juoserver.game.damage.DamageModule;
import com.github.mayconr.juoserver.game.damage.DamageModuleImpl;
import com.github.mayconr.juoserver.game.economy.EconomyModule;
import com.github.mayconr.juoserver.game.economy.EconomyModuleImpl;
import com.github.mayconr.juoserver.game.economy.StockHandler;
import com.github.mayconr.juoserver.game.economy.VendorHandler;
import com.github.mayconr.juoserver.game.interaction.InteractionModuleImpl;
import com.github.mayconr.juoserver.game.interaction.InteractionModule;
import com.github.mayconr.juoserver.game.interaction.action.ActionHandler;
import com.github.mayconr.juoserver.game.interaction.animation.AnimationHandler;
import com.github.mayconr.juoserver.game.interaction.speech.SpeechHandler;
import com.github.mayconr.juoserver.game.item.*;
import com.github.mayconr.juoserver.game.item.template.ItemTemplate;
import com.github.mayconr.juoserver.game.item.template.ItemTemplateRegistry;
import com.github.mayconr.juoserver.game.item.trigger.ItemUseService;
import com.github.mayconr.juoserver.game.messaging.MessageModule;
import com.github.mayconr.juoserver.game.messaging.MessageModuleImpl;
import com.github.mayconr.juoserver.game.messaging.template.MessageStyleTemplate;
import com.github.mayconr.juoserver.game.mobile.MobileModule;
import com.github.mayconr.juoserver.game.mobile.MobileModuleImpl;
import com.github.mayconr.juoserver.game.mobile.npc.NpcDespawnService;
import com.github.mayconr.juoserver.game.mobile.template.MountTemplate;
import com.github.mayconr.juoserver.game.mobile.template.NpcTemplate;
import com.github.mayconr.juoserver.game.npc.stats.NpcStatsResolver;
import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.game.model.event.*;
import com.github.mayconr.juoserver.game.npc.NpcModule;
import com.github.mayconr.juoserver.game.npc.NpcModuleImpl;
import com.github.mayconr.juoserver.game.player.PlayerModule;
import com.github.mayconr.juoserver.game.player.PlayerCommands;
import com.github.mayconr.juoserver.game.player.PlayerVitalsHandler;
import com.github.mayconr.juoserver.game.player.template.BodyKey;
import com.github.mayconr.juoserver.game.player.template.BodyTemplate;
import com.github.mayconr.juoserver.game.player.template.StartKitTemplate;
import com.github.mayconr.juoserver.game.skill.SkillHandler;
import com.github.mayconr.juoserver.game.skill.SkillModule;
import com.github.mayconr.juoserver.game.skill.SkillModuleImpl;
import com.github.mayconr.juoserver.game.ui.*;
import com.github.mayconr.juoserver.game.ui.gump.DefaultGumpSystem;
import com.github.mayconr.juoserver.game.wallet.Wallet;
import com.github.mayconr.juoserver.game.world.context.DefaultFlowFacade;
import com.github.mayconr.juoserver.game.world.context.DefaultModuleContext;
import com.github.mayconr.juoserver.game.world.context.FlowRegistry;
import com.github.mayconr.juoserver.game.world.context.FlowRegistryFactory;
import com.github.mayconr.juoserver.game.world.context.FlowRegistryFactory.GameInfra;
import com.github.mayconr.juoserver.game.world.context.FlowRegistryFactory.GameModules;
import com.github.mayconr.juoserver.game.world.context.FlowRegistryFactory.GameTemplates;
import com.github.mayconr.juoserver.game.world.transition.DespawnNpcOnDeath;
import com.github.mayconr.juoserver.game.world.transition.RegionTransitionServiceImpl;
import com.github.mayconr.juoserver.game.world.transition.TeleportTransitionServiceImpl;
import com.github.mayconr.juoserver.game.world.transition.VisibilityTransitionServiceImpl;
import com.github.mayconr.juoserver.infrastructure.datafile.UOFileReaderImpl;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.flow.FlowExecutor;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractContext;
import com.github.mayconr.juoserver.infrastructure.flow.Flow;
import com.github.mayconr.juoserver.infrastructure.policy.PolicyService;
import com.github.mayconr.juoserver.infrastructure.region.RegionSystem;
import com.github.mayconr.juoserver.infrastructure.rng.RNG;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;
import com.github.mayconr.juoserver.infrastructure.template.InMemoryTemplateRegistry;
import com.github.mayconr.juoserver.infrastructure.template.TemplateRegistry;
import com.github.mayconr.juoserver.network.packet.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
@RequiredArgsConstructor
public class DefaultWorld implements WorldInternal, World {

    @Override
    public WorldStorage storage() {
        return Objects.requireNonNull(storageModule, "World storage module is not initialized");
    }

    @Override
    public WorldRandom random() {
        return Objects.requireNonNull(randomModule, "World random module is not initialized");
    }

    @Override
    public WorldAI ai() {
        return Objects.requireNonNull(aiModule, "World AI module is not initialized");
    }

    @Override
    public EconomyModule economy() {
        return Objects.requireNonNull(economyModule, "World economy module is not initialized");
    }

    @Override
    public PlayerCommands player() {
        return Objects.requireNonNull(playerModule, "World player module is not initialized");
    }

    @Override
    public WorldCombat combat() {
        return Objects.requireNonNull(combatModule, "World combat module is not initialized");
    }

    @Override
    public ItemModule item() {
        return Objects.requireNonNull(itemModule, "World item module is not initialized");
    }

    private MapModule mapModule;

    @Override
    public WorldMap map() {
        return Objects.requireNonNull(mapModule, "World map module is not initialized");
    }

    @Override
    public InteractionModule interaction() {
        return Objects.requireNonNull(interactionModule, "World interaction module is not initialized");
    }

    @Override
    public MobileModule mobile() {
        return Objects.requireNonNull(mobileModule, "World mobile module is not initialized");
    }

    @Override
    public UICommands ui() {
        return Objects.requireNonNull(uiModule, "World UI module is not initialized");
    }

    @Override
    public SkillModule skill() {
        return Objects.requireNonNull(skillModule, "World skill module is not initialized");
    }

    @Override
    public WorldDamage damage() {
        return Objects.requireNonNull(damageModule, "World damage module is not initialized");
    }

    @Override
    public WorldSpell spell() {
        return Objects.requireNonNull(spellModule, "World spell module is not initialized");
    }

    @Override
    public WorldNpc npc() {
        return Objects.requireNonNull(npcModule, "World NPC module is not initialized");
    }

    @Override
    public WorldMessage message() {
        return Objects.requireNonNull(messageModule, "World message module is not initialized");
    }

    /*
     * =========
     * Modules
     * =========
     */
    @Override
    public WorldDevTools devTools() {
        return Objects.requireNonNull(devToolsModule, "World dev tools module is not initialized");
    }

    private StorageModule storageModule;
    private RandomModule randomModule;
    private DevToolsModule devToolsModule;
    private EconomyModule economyModule;
    private AIModule aiModule;
    private UIModule uiModule;
    private SkillModule skillModule;
    private SpellModule spellModule;
    private ItemModule itemModule;
    private PlayerModule playerModule;
    private CombatModule combatModule;
    private MobileModule mobileModule;
    private InteractionModule interactionModule;
    private MessageModule messageModule;
    private DamageModule damageModule;
    private NpcModule npcModule;

    /*
     * =========
     * Systems
     * =========
     */
    private final EventBus eventBus;
    private final SerialGenerator serialGenerator;
    private final RealmStorage storage;
    private final RegionSystem regionSystem;
    private final UOFileReaderImpl fileReader;
    private final PolicyService policyService;
    private final ItemUseService itemUseService;
    private final SpellCastRegistry spellCastRegistry;
    private final RNG rng;

    /*
     * ==========
     * Templates
     * ==========
     */
    private final ItemTemplateRegistry itemTemplateRegistry;
    private final TemplateRegistry<String, NpcTemplate> npcTemplateByName;
    private final NpcStatsResolver npcStatsResolver;
    private final TemplateRegistry<String, ItemTemplate> itemTemplateByName;
    private final TemplateRegistry<Integer, ItemTemplate> itemTemplateByModelId;
    private final TemplateRegistry<BodyKey, BodyTemplate> bodyTemplateByBodyKey;
    private final TemplateRegistry<Integer, StartKitTemplate> startKitTemplateBySkillId;
    private final TemplateRegistry<String, MountTemplate> mountTemplateByNpcName;
    private final TemplateRegistry<String, MountTemplate> mountTemplateByItemName;
    private final TemplateRegistry<String, SpellTemplate> spellTemplateByKey;
    /*
     * ==========
     * Properties
     * ==========
     */

    private final GamePlaySettings settings;
    private final WorldCfg worldCfg;

    @Override
    public void initialize() {
        this.storageModule = new StorageModuleImpl(storage);
        this.randomModule = new RandomModuleImpl(rng);
        serialGenerator.initialize();
        fileReader.loadFiles();
        this.mapModule = new MapModule(fileReader, regionSystem);

        final var wallet = worldCfg.wallet().apply(this);

        this.devToolsModule = new DevToolsModuleImpl(eventBus);
        initializeMessagingModule();
        initializeEconomyModule(wallet);
        initializeAiModule();
        initializeUiModule();
        initializeSkillModule();
        this.spellModule = new SpellModuleImpl(spellTemplateByKey);
        initializeItemModule();
        initializePlayerModule();
        initializeCombatModule();
        initializeMobileModule(wallet);
        initializeInteractionModule();
        initializeDamageModule();
        initializeNpcModule();

        initializeModules();
        registerTransitions();
        initializeStorage();
    }

    public void update(double delta) {
        aiModule.update(delta);
        playerModule.update(delta);
        mobileModule.update(delta);
        combatModule.update(delta);
        spellModule.update(delta);
    }

    /*
     * =====================
     * Module Initialization
     * =====================
     */

    private void initializeMessagingModule() {
        final var styles = worldCfg.content().messageStyles().loadAll();
        var messageStyleRegistry = new InMemoryTemplateRegistry<>(styles, MessageStyleTemplate::name);
        this.messageModule = new MessageModuleImpl(eventBus, messageStyleRegistry);
    }

    private void initializeEconomyModule(Wallet wallet) {
        final var pricingStrategy = worldCfg.pricingStrategy().get();
        final var vendorHandler = new VendorHandler(eventBus, serialGenerator, pricingStrategy);
        final var stockHandler = new StockHandler();
        final var templateLoader = worldCfg.content().stocks();

        this.economyModule = new EconomyModuleImpl(eventBus, vendorHandler, stockHandler, wallet, templateLoader);
    }

    private void initializeAiModule() {
        //final var aiFactory = new NpcAiRegistry(worldCfg.aiList());
        //final var profileRegistry = new BehaviorProfileRegistry(worldCfg.behaviorProfileList());
        //final var aiSessionHandler = new AISessionManager(eventBus, profileRegistry, aiFactory);
        var engine = new AIEngineImpl(this, e->{

            switch (e) {
                //case SpeechAction say -> world.message().printTextAbove(, say.content(), say.speechTo());
                case WalkAction walkAction -> mobile().move(walkAction.npc(), walkAction.direction());
                case SellListAction buyList -> {
                    var region = map().getRegion(buyList.buyer())
                            .orElseThrow(() -> new RuntimeException("Region not found"));
                    economy().beginVendorPurchase(buyList.buyer(), buyList.seller(), region, buyList.itemsToSell());
                }
                case SpeechAction speech -> message().printTextAbove(speech.speaker(), speech.content(), speech.target());
                default -> throw new IllegalStateException("Unexpected value: " + e);
            }

        });
        this.aiModule = new AIModuleImpl(engine, eventBus);
    }

    private void initializeUiModule() {
        final var tooltipHandler = new TooltipHandler(eventBus, storage);
        final var doubleClickHandler = new DoubleClickHandler(eventBus, storage, itemUseService, policyService);
        final var singleClickHandler = new SingleClickHandler(storage);
        final var skillUIHandler = new SkillUIHandler(eventBus, storage);
        final var statusHandler = new StatusHandler(eventBus, storage);
        final var gumpSystem = new DefaultGumpSystem(eventBus);

        this.uiModule = new UIModule(
                tooltipHandler,
                doubleClickHandler,
                singleClickHandler,
                skillUIHandler,
                gumpSystem,
                statusHandler
        );
    }

    private void initializeSkillModule() {
        final var skillSystem = Objects.requireNonNull(
                worldCfg.skillSystem().create(settings, rng, eventBus), "Skill system factory returned null");
        final var skillHandler = new SkillHandler(eventBus);

        this.skillModule = new SkillModuleImpl(skillHandler, skillSystem);
    }

    private void initializeItemModule() {
        final var itemHandler = new ItemHandler(storage, eventBus);
        final var containerHandler = new ContainerHandler(eventBus, storage);

        this.itemModule = new ItemModuleImpl(itemHandler, containerHandler, storage, itemTemplateRegistry);
    }

    private void initializePlayerModule() {
        final var vitals = new PlayerVitalsHandler(this);
        this.playerModule = new PlayerModule(vitals, storage, eventBus);
    }

    private void initializeCombatModule() {
        final var vitalsService = new VitalsHandler(eventBus, settings);
        final var combatService = new CombatHandler(eventBus);

        this.combatModule = new CombatModuleImpl(combatService, vitalsService);
    }

    private void initializeMobileModule(Wallet wallet) {
        this.npcModule = new NpcModuleImpl();
        final var npcDespawnService = new NpcDespawnService(storage, npcModule);

        this.mobileModule = new MobileModuleImpl(npcDespawnService, wallet, eventBus, storage);
    }

    private void initializeInteractionModule() {
        final var actionHandler = new ActionHandler(eventBus);
        final var animationService = new AnimationHandler(eventBus);
        final var speechHandler = new SpeechHandler(eventBus);

        this.interactionModule = new InteractionModuleImpl(actionHandler, animationService, speechHandler);
    }

    private void initializeDamageModule() {
        damageModule = new DamageModuleImpl(eventBus);
    }

    private void initializeNpcModule() {
    }

    /*
     * ===================
     * Module initialization
     * ===================
     */

    private FlowRegistry flowRegistry;
    private DefaultFlowFacade flowFacade;

    public FlowExecutor flows() {
        return flowFacade;
    }

    /** Called by bootstrap after module initialization and before enabling world updates. */
    public <T extends AbstractContext> void registerFlow(Class<T> contextType, Flow<T> flow) {
        flowRegistry.register(contextType.getName(), flow, contextType);
    }

    private void initializeModules() {
        this.flowRegistry = FlowRegistryFactory.builder()
                .modules(GameModules.builder()
                    .message(messageModule)
                    .ai(aiModule)
                    .npc(npcModule)
                    .mobile(mobileModule)
                    .item(itemModule)
                    .damage(damageModule)
                    .skill(skillModule)
                    .build())
                .infra(GameInfra.builder()
                    .spellCastRegistry(spellCastRegistry)
                    .serialGenerator(serialGenerator)
                    .eventBus(eventBus)
                    .storage(storage)
                    .settings(settings)
                    .fileReader(fileReader)
                    .rng(rng)
                    .combatSkillGainPolicy(Objects.requireNonNull(
                            worldCfg.combatSkillGainPolicy().get(), "Combat skill gain policy factory returned null"))
                    .build())
                .templates(GameTemplates.builder()
                    .spellByKey(spellTemplateByKey)
                    .itemByModelId(itemTemplateByModelId)
                    .itemByName(itemTemplateByName)
                    .npcByName(npcTemplateByName)
                    .npcStatsResolver(npcStatsResolver)
                    .bodyByKey(bodyTemplateByBodyKey)
                    .startKitBySkillId(startKitTemplateBySkillId)
                    .mountByItemName(mountTemplateByItemName)
                    .mountByNpcName(mountTemplateByNpcName)
                    .build())
                .build()
                .buildRegistry();

        this.flowFacade = DefaultFlowFacade.builder()
                .registry(flowRegistry)
                .build();
        final var context = DefaultModuleContext.builder()
                .flowFacade(flowFacade)
                .build();

        this.storageModule.initialize(context);
        this.randomModule.initialize(context);
        this.devToolsModule.initialize(context);
        this.economyModule.initialize(context);
        this.mobileModule.initialize(context);
        this.playerModule.initialize(context);
        this.damageModule.initialize(context);
        this.npcModule.initialize(context);
        this.itemModule.initialize(context);
        this.aiModule.initialize(context);
        this.interactionModule.initialize(context);
        this.skillModule.initialize(context);
        this.spellModule.initialize(context);
        this.combatModule.initialize(context);
    }

    /*
     * ===================
     * Transition Registration
     * ===================
     */

    private void registerTransitions() {
        /*
         * ==========
         * Transitions
         * ==========
         */
        final var visibilityTransitionService = new VisibilityTransitionServiceImpl(storage, eventBus, settings);
        final var regionTransitionService = new RegionTransitionServiceImpl(regionSystem, eventBus);
        final var teleportTransitionService = new TeleportTransitionServiceImpl(mobileModule);
        final var despawnNpcOnDeath = new DespawnNpcOnDeath(mobileModule, aiModule);

        eventBus.register(MobileMoved.class, visibilityTransitionService);
        eventBus.register(MobileMoved.class, regionTransitionService);
        eventBus.register(teleportTransitionService);
        eventBus.register(despawnNpcOnDeath);
        eventBus.register(PlayerSessionStatusChanged.class, this::handleSessionStateChanged);
        eventBus.register(PlayerSessionClosed.class, this::handleSessionClosed);

        eventBus.register(
                ItemDroppedInContainer.class,
                event -> mobileModule.recalculateGold(event.player()),
                event -> wallet().isGold(event.item())
        );

        eventBus.register(
                ItemCreatedInContainer.class,
                event -> {},//mobileModule.recalculateGold(((UOItem) event.container()).getOwner()),
                event -> wallet().isGold(event.item()) && event.container() instanceof UOItem item && item.getCurrentLocation() instanceof EquippedLocation
        );
    }

    private void initializeStorage() {
        this.storage.initialize(
                serialGenerator::getCurrentItem,
                serialGenerator::getCurrentMobile,
                data -> {
                    for (UOMobile mobile : data.mobiles()) {
                        if (mobile instanceof UONpc npc) {
                            /*var ai = aiModule.attach(npc);
                            if (ai != null) {
                                ai.wakeup(this);
                            }*/
                        }
                    }
                }
        );
    }

    private Wallet wallet() {
        return worldCfg.wallet().apply(this);
    }

    /*
     * ==============
     * Event Handlers
     * ==============
     */

    private void handleSessionStateChanged(PlayerSessionStatusChanged event) {
        switch (event.newState()) {
            case ACTIVE -> playerModule.spawn(event.session().getPlayer());
        }
    }

    /**
     * Handles the player session closure event.
     *
     * <p>Despawns the associated player from the game world.</p>
     *
     * @param event session closure event
     */
    private void handleSessionClosed(PlayerSessionClosed event) {
        final var player = event.session().getPlayer();
        if (player != null) {
            playerModule.despawn(player);
        }
    }

}
