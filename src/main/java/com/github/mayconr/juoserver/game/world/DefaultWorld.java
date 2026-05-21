package com.github.mayconr.juoserver.game.world;

import com.github.mayconr.juoforge.reader.view.LandTile;
import com.github.mayconr.juoforge.reader.view.StaticTile;
import com.github.mayconr.juoserver.WorldCfg;
import com.github.mayconr.juoserver.game.GamePlaySettings;
import com.github.mayconr.juoserver.game.ai.AIEngineImpl;
import com.github.mayconr.juoserver.game.ai.AIModule;
import com.github.mayconr.juoserver.game.ai.AIModuleImpl;
import com.github.mayconr.juoserver.game.ai.actions.SellListAction;
import com.github.mayconr.juoserver.game.ai.actions.SpeechAction;
import com.github.mayconr.juoserver.game.ai.actions.WalkAction;
import com.github.mayconr.juoserver.game.combat.CombatHandler;
import com.github.mayconr.juoserver.game.combat.CombatModule;
import com.github.mayconr.juoserver.game.combat.DefaultCombatSystem;
import com.github.mayconr.juoserver.game.combat.VitalsHandler;
import com.github.mayconr.juoserver.game.damage.DamageModule;
import com.github.mayconr.juoserver.game.damage.DamageModuleImpl;
import com.github.mayconr.juoserver.game.economy.EconomyModule;
import com.github.mayconr.juoserver.game.economy.EconomyModuleImpl;
import com.github.mayconr.juoserver.game.economy.StockHandler;
import com.github.mayconr.juoserver.game.economy.VendorHandler;
import com.github.mayconr.juoserver.game.economy.stock.StockEntry;
import com.github.mayconr.juoserver.game.economy.template.RegionStockTemplate;
import com.github.mayconr.juoserver.game.interaction.InteractionModuleImpl;
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
import com.github.mayconr.juoserver.game.mobile.template.NpcTemplate;
import com.github.mayconr.juoserver.game.mobile.template.MountTemplate;
import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.game.model.event.*;
import com.github.mayconr.juoserver.game.model.event.message.MessageContent;
import com.github.mayconr.juoserver.game.npc.NpcModule;
import com.github.mayconr.juoserver.game.npc.NpcModuleImpl;
import com.github.mayconr.juoserver.game.player.PlayerModule;
import com.github.mayconr.juoserver.game.player.PlayerVitalsHandler;
import com.github.mayconr.juoserver.game.player.template.BodyKey;
import com.github.mayconr.juoserver.game.player.template.BodyTemplate;
import com.github.mayconr.juoserver.game.player.template.StartKitTemplate;
import com.github.mayconr.juoserver.game.skill.DefaultSkillSystem;
import com.github.mayconr.juoserver.game.skill.SkillHandler;
import com.github.mayconr.juoserver.game.skill.SkillModule;
import com.github.mayconr.juoserver.game.skill.SkillModuleImpl;
import com.github.mayconr.juoserver.game.ui.*;
import com.github.mayconr.juoserver.game.ui.gump.DeclarativeGumpUI;
import com.github.mayconr.juoserver.game.ui.gump.DefaultGumpSystem;
import com.github.mayconr.juoserver.game.ui.gump.GumpHandler;
import com.github.mayconr.juoserver.game.wallet.Wallet;
import com.github.mayconr.juoserver.game.world.context.DefaultFlowFacade;
import com.github.mayconr.juoserver.game.world.context.DefaultModuleContext;
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
import com.github.mayconr.juoserver.infrastructure.gameloop.GameLoop;
import com.github.mayconr.juoserver.infrastructure.gameloop.GameTask;
import com.github.mayconr.juoserver.infrastructure.policy.PolicyService;
import com.github.mayconr.juoserver.infrastructure.region.RegionNode;
import com.github.mayconr.juoserver.infrastructure.region.RegionSystem;
import com.github.mayconr.juoserver.infrastructure.rng.RNG;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;
import com.github.mayconr.juoserver.infrastructure.template.InMemoryTemplateRegistry;
import com.github.mayconr.juoserver.infrastructure.template.JsonTemplateLoader;
import com.github.mayconr.juoserver.infrastructure.template.TemplateRegistry;
import com.github.mayconr.juoserver.network.packet.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Predicate;

@Slf4j
@RequiredArgsConstructor
public class DefaultWorld implements WorldInternal, World {

    /*
     * =========
     * Modules
     * =========
     */
    private EconomyModule economyModule;
    private AIModule aiModule;
    private UIModule uiModule;
    private SkillModule skillModule;
    private ItemModule itemModule;
    private PlayerModule playerModule;
    private CombatModule combatModule;
    private MobileModule mobileModule;
    private InteractionModuleImpl interactionModule;
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
    private final GameLoop gameLoop;
    private final RegionSystem regionSystem;
    private final UOFileReaderImpl fileReader;
    private final PolicyService policyService;
    private final ItemUseService itemUseService;
    private final RNG rng;

    /*
     * ==========
     * Templates
     * ==========
     */
    private final ItemTemplateRegistry itemTemplateRegistry;
    private final TemplateRegistry<String, NpcTemplate> npcTemplateByName;
    private final TemplateRegistry<String, ItemTemplate> itemTemplateByName;
    private final TemplateRegistry<Integer, ItemTemplate> itemTemplateByModelId;
    private final TemplateRegistry<BodyKey, BodyTemplate> bodyTemplateByBodyKey;
    private final TemplateRegistry<Integer, StartKitTemplate> startKitTemplateBySkillId;
    private final TemplateRegistry<String, MountTemplate> mountTemplateByNpcName;
    private final TemplateRegistry<String, MountTemplate> mountTemplateByItemName;
    /*
     * ==========
     * Properties
     * ==========
     */

    private final GamePlaySettings settings;
    private final WorldCfg worldCfg;

    @Override
    public void initialize() {
        serialGenerator.initialize();
        fileReader.loadFiles();

        final var wallet = worldCfg.wallet().apply(this);

        initializeMessagingModule();
        initializeEconomyModule(wallet);
        initializeAiModule();
        initializeUiModule();
        initializeSkillModule();
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
    }

    /*
     * =====================
     * Module Initialization
     * =====================
     */

    private void initializeMessagingModule() {
        final var styles = new JsonTemplateLoader<>(Path.of("template/config/message-styles.json"), MessageStyleTemplate.class).load().values();
        var messageStyleRegistry = new InMemoryTemplateRegistry<>(styles, MessageStyleTemplate::name);
        this.messageModule = new MessageModuleImpl(eventBus, messageStyleRegistry);
    }

    private void initializeEconomyModule(Wallet wallet) {
        final var pricingStrategy = worldCfg.pricingStrategy().get();
        final var vendorHandler = new VendorHandler(eventBus, serialGenerator, pricingStrategy);
        final var stockHandler = new StockHandler();
        final var templateLoader = new JsonTemplateLoader<>(Path.of("template/stock"), RegionStockTemplate.class);

        this.economyModule = new EconomyModuleImpl(vendorHandler, stockHandler, wallet, templateLoader);
    }

    private void initializeAiModule() {
        //final var aiFactory = new NpcAiRegistry(worldCfg.aiList());
        //final var profileRegistry = new BehaviorProfileRegistry(worldCfg.behaviorProfileList());
        //final var aiSessionHandler = new AISessionManager(eventBus, profileRegistry, aiFactory);
        var engine = new AIEngineImpl(this, e->{

            switch (e) {
                //case SpeechAction say -> world.printTextAbove(, say.content(), say.speechTo());
                case WalkAction walkAction -> move(walkAction.npc(), walkAction.direction());
                case SellListAction buyList -> beginVendorPurchase(buyList.buyer(), buyList.seller(), buyList.itemsToSell());
                case SpeechAction speech -> printTextAbove(speech.speaker(), speech.content(), speech.target());
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
        final var skillSystem = new DefaultSkillSystem(settings, rng, eventBus);
        final var skillHandler = new SkillHandler(eventBus);

        this.skillModule = new SkillModuleImpl(skillHandler, skillSystem);
    }

    private void initializeItemModule() {
        final var itemHandler = new ItemHandler(serialGenerator, itemTemplateRegistry, storage, eventBus);
        final var containerHandler = new ContainerHandler(eventBus, storage);

        this.itemModule = new ItemModuleImpl(itemHandler, containerHandler);
    }

    private void initializePlayerModule() {
        final var vitals = new PlayerVitalsHandler(this);
        this.playerModule = new PlayerModule(vitals, storage, eventBus);
    }

    private void initializeCombatModule() {
        final var vitalsService = new VitalsHandler(eventBus, settings);
        final var combatSystem = new DefaultCombatSystem(null);
        final var combatService = new CombatHandler(eventBus, combatSystem, storage);

        this.combatModule = new CombatModule(combatService, vitalsService);
    }

    private void initializeMobileModule(Wallet wallet) {
        final var npcDespawnService = new NpcDespawnService(storage);

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
        this.npcModule = new NpcModuleImpl();
    }

    /*
     * ===================
     * Module initialization
     * ===================
     */

    private void initializeModules() {
        final var flowRegistry = FlowRegistryFactory.builder()
                .modules(GameModules.builder()
                    .message(messageModule)
                    .ai(aiModule)
                    .npc(npcModule)
                    .mobile(mobileModule)
                    .item(itemModule)
                    .build())
                .infra(GameInfra.builder()
                    .serialGenerator(serialGenerator)
                    .eventBus(eventBus)
                    .storage(storage)
                    .settings(settings)
                    .fileReader(fileReader)
                    .build())
                .templates(GameTemplates.builder()
                    .itemByModelId(itemTemplateByModelId)
                    .itemByName(itemTemplateByName)
                    .npcByName(npcTemplateByName)
                    .bodyByKey(bodyTemplateByBodyKey)
                    .startKitBySkillId(startKitTemplateBySkillId)
                    .mountByItemName(mountTemplateByItemName)
                    .mountByNpcName(mountTemplateByNpcName)
                    .build())
                .build()
                .buildRegistry();

        final var flowFacade = DefaultFlowFacade.builder()
                .registry(flowRegistry)
                .build();
        final var context = DefaultModuleContext.builder()
                .flowFacade(flowFacade)
                .build();

        this.economyModule.initialize(context);
        this.mobileModule.initialize(context);
        this.playerModule.initialize(context);
        this.damageModule.initialize(context);
        this.npcModule.initialize(context);
        this.itemModule.initialize(context);
        this.aiModule.initialize(context);
        this.interactionModule.initialize(context);
        this.skillModule.initialize(context);
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

    /*
     * ======================
     * Loading / World Query
     * ======================
     */

    @Override
    public CompletableFuture<UOMobile> loadMobile(int serialId) {
        if (!UOMobile.isMobile(serialId)) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("Serial [" + serialId + "] is not a player"));
        }
        return storage.loadMobile(serialId);
    }

    @Override
    public List<UOMobile> getMobilesInRange(Location location, int radius, Predicate<UOMobile> filter) {
        return storage.getMobilesInRange(location, radius, filter);
    }

    @Override
    public List<UOItem> getItemsInRange(Location location, int radius) {
        return storage.getItemsInRange(location);
    }

    @Override
    public List<UOItem> getItemsInContainer(Integer container, Predicate<UOItem> predicate) {
        return itemModule.getItemsInContainer(container, predicate);
    }

    @Override
    public Map<Layer, UOItem> getEquippedItems(UOMobile mobile) {
        return mobileModule.getEquippedItems(mobile);
    }

    @Override
    public boolean isMobile(int serialId) {
        return UOMobile.isMobile(serialId);
    }

    @Override
    public Optional<UOMobile> getMobileBySerialId(int serial) {
        return storage.getMobile(serial);
    }

    @Override
    public Optional<UOItem> getItemBySerialId(int serial) {
        return storage.getItem(serial);
    }

    @Override
    public Optional<UOContainer> getContainerBySerialId(int serial) {
        return storage.getContainer(serial);
    }

    @Override
    public Optional<RegionNode> getRegion(String name) {
        return regionSystem.getRegion(name);
    }

    @Override
    public Optional<RegionNode> getRegion(Location location) {
        return regionSystem.getRegion(location);
    }

    @Override
    public List<RegionNode> getRegionsByType(RegionType type) {
        return regionSystem.getRegionsByType(type);
    }

    @Override
    public CompletableFuture<List<AccountMobile>> getPlayerMobiles(UOAccount uoAccount) {
        return storage.getAccountMobiles(uoAccount);
    }

    /*
     * ===========
     * Map / Files
     * ===========
     */

    @Override
    public List<StaticTile> getStatics(Location location) {
        return fileReader.getStatics(location);
    }

    @Override
    public List<StaticTile> getStatics(int x, int y) {
        return fileReader.getStatics(x, y);
    }

    @Override
    public LandTile getLandTile(int x, int y) {
        return fileReader.getLandTile(x, y);
    }

    @Override
    public LandTile getLandTile(Location location) {
        return fileReader.getLandTile(location);
    }

    /*
     * =================
     * Interaction / UI
     * =================
     */

    @Override
    public void sendAnimation(UOMobile mobile, AnimationOptions options) {
        interactionModule.sendAnimation(mobile, options);
    }

    @Override
    public void sendTarget(UOPlayer player, CursorType type, Consumer<TargetResult> consumer) {
        interactionModule.sendTarget(player, type, consumer);
    }

    @Override
    public void resolveTarget(UOPlayer player, Target target) {
        interactionModule.resolveTarget(player, target);
    }

    @Override
    public void speech(UOPlayer player, UnicodeSpeachRequest request) {
        interactionModule.speech(player, request);
    }

    @Override
    public void playerStatusRequested(UOPlayer player, GetPlayerStatus getPlayerStatus) {
        switch (getPlayerStatus.getType()) {
            case BASIC_STATUS -> uiModule.sendStatusGump(player, getPlayerStatus.getSerialId());
            case REQUEST_SKILL -> uiModule.sendSkillGump(player, getPlayerStatus.getSerialId());
            case GOD_CLIENT -> System.out.println("god client");
        }
    }

    @Override
    public void tooltipRequest(UOPlayer player, List<Integer> serials) {
        uiModule.tooltipRequest(player, serials);
    }

    @Override
    public void doubleClick(UOPlayer player, DoubleClick doubleClick) {
        uiModule.doubleClick(player, doubleClick);
    }

    @Override
    public void singleClick(UOPlayer player, SingleClickRequest singleClick) {
        uiModule.singleClick(player, singleClick);
    }

    @Override
    public void handleAction(UOPlayer player, ActionRequest request) {
        interactionModule.handleAction(player, request);
    }

    @Override
    public void sendGump(UOPlayer player, DeclarativeGumpUI gumpUI, GumpHandler handler) {
        uiModule.sendGump(player, gumpUI, handler);
    }

    @Override
    public void gumpResponse(UOPlayer player, GumpSelection gumpSelection) {
        uiModule.onGumpSelection(player, gumpSelection);
    }

    /*
     * =================
     * Messaging
     * =================
     */
    @Override
    public void sendMessage(UOPlayer player, MessageContent content) {
        messageModule.send(player, content);
    }

    @Override
    public void sendMessage(UOPlayer player, String message) {
        messageModule.send(player, message);
    }

    @Override
    public void printTextAbove(UOObject source, MessageContent content) {
        messageModule.printTextAbove(source, content);
    }

    @Override
    public void printTextAbove(UOObject source, MessageContent content, UOPlayer player) {
        messageModule.printTextAbove(source, content, player);
    }

    @Override
    public void broadcast(MessageContent message) {
        messageModule.broadcast(message);
    }

    /*
     * ========
     * Movement
     * ========
     */
    @Override
    public void move(UOMobile mobile, MoveRequest moveRequest) {
        mobileModule.move(mobile, moveRequest);
    }

    @Override
    public void move(UOMobile mobile, Direction direction) {
        mobileModule.move(mobile, direction);
    }

    @Override
    public void teleport(UOMobile mobile, Location location) {
        mobileModule.teleport(mobile, location);
    }

    @Override
    public void resync(UOPlayer player, MoveResyncAck resyncAck) {
        mobileModule.resync(player, resyncAck);
    }

    /*
     * =====
     * Items
     * =====
     */

    @Override
    public void deleteItem(int serial) {
        if (!UOItem.isItem(serial)) {
            throw new IllegalArgumentException("Serial [" + serial + "] is not an item");
        }

        final var item = storage.getItem(serial)
                .orElseThrow(() -> new IllegalArgumentException("Item [" + serial + "] not found"));

        itemModule.deleteItem(item);
    }

    @Override
    public void deleteItem(UOItem item) {
        itemModule.deleteItem(item);
    }

    @Override
    public void equipItem(UOPlayer player, EquipItemRequest equipItem) {
        getItemBySerialId(equipItem.getItemSerialId())
                .ifPresent(item -> mobileModule.equipItem(player, item));
    }

    @Override
    public void unequipItem(UOPlayer player, UnequipItem pickedUpItem) {
        mobileModule.unequipItem(player, pickedUpItem);
    }

    @Override
    public void dropItem(UOPlayer player, DropItem dropItem) {
        itemModule.dropItem(player, dropItem);
    }

    @Override
    public UOItem createItem(ItemRequest request, ItemTarget target) {
        return itemModule.createItem(request, target);
    }

    @Override
    public ConsumeResult consumeItem(Integer containerSerial, String itemName, int amount, boolean searchNestedContainers) {
        return itemModule.consumeItem(containerSerial, itemName, amount, searchNestedContainers);
    }

    @Override
    public List<ItemTemplate> getItemsTemplate(String stockType) {
        return itemTemplateRegistry.getItemTemplates(stockType);
    }

    /*
     * =======
     * Players
     * =======
     */

    @Override
    public CompletableFuture<UOPlayer> createPlayerMobile(
            CreateCharacter character,
            Map<Integer, RegionNode> startingLocations,
            UOAccount account
    ) {
        return playerModule.createPlayerMobile(character, startingLocations, account);
    }

    @Override
    public CompletableFuture<Void> deletePlayerMobile(int serialId) {
        return playerModule.deletePlayerMobile(serialId);
    }

    @Override
    public List<UOPlayer> getOnlinePlayers() {
        return Collections.emptyList();
    }

    /*
     * ====
     * NPCs
     * ====
     */

    @Override
    public UONpc createNpc(String template, Location location) {
        return npcModule.createNpc(template, location);
    }

    @Override
    public void deleteMobile(UOMobile mobile) {
        switch (mobile) {
            case UONpc npc -> npcModule.removeNpc(npc);
            case UOPlayer player -> log.info("Remove a player is not allowed yet {}", player.getId());
            default -> throw new IllegalStateException("Unexpected value: " + mobile);
        }
    }

    @Override
    public void applyDamage(DamageRequest request) {
        damageModule.applyDamage(request);
    }

    @Override
    public void kill(UOMobile target, UOMobile source, DamageSourceKind kind) {
        damageModule.kill(target, source, kind);
    }

    @Override
    public void resurrect(UOMobile mobile) {
        mobileModule.resurrect(mobile);
    }

    /*
     * ========
     * Skills
     * ========
     */

    @Override
    public void tryGainSkill(UOMobile mobile, int skillId, double difficulty, SkillGainContext context) {
        skillModule.tryGain(mobile, skillId, difficulty, context);
    }

    @Override
    public void useSkill(UOPlayer player, int skillId) {
        skillModule.useSkill(player, skillId);
    }

    @Override
    public void sendSkillsLock(UOPlayer player, Collection<SkillValue> skills) {
        skillModule.sendSkillsLock(player, skills);
    }

    /*
     * ========
     * Combat
     * ========
     */

    @Override
    public void toggleWarMode(UOPlayer player, WarModeType type) {
        combatModule.toggleWarMode(player, type);
    }

    @Override
    public void attack(UOPlayer player, AttackRequest request) {
        combatModule.attack(player, request);
    }

    @Override
    public void regen(UOMobile mobile, double interval) {
        combatModule.regen(mobile, interval);
    }

    /*
     * ========
     * Vendors
     * ========
     */

    @Override
    public void beginVendorPurchase(UOPlayer player, UOMobile vendor, List<StockEntry> items) {
        var region = regionSystem.getRegion(player)
                .orElseThrow(() -> new RuntimeException("Region not found"));

        economyModule.beginVendorPurchase(player, vendor, region, items);
    }

    @Override
    public void completeVendorPurchase(UOPlayer player, VendorBuyRequest vendorBuyRequest) {
        var result = economyModule.resolveVendorPurchase(player, vendorBuyRequest);

        if (!result.success()) {
            eventBus.publish(new VendorPurchaseFailed(player));
            return;
        }

        final List<UOItem> items = new ArrayList<>();
        for (var item : result.items()) {
            // TODO get backpack by serial
            /*items.add(itemModule.createItem(
                    ItemRequest.byTemplate(item.template()),
                    ContainerItemTarget.of(player.getBackpack())
            ));*/
        }

        eventBus.publish(new VendorPurchaseCompleted(player, items));
    }

    @Override
    public Optional<StockEntry> getStockEntry(ItemTemplate template, RegionNode regionNode) {
        return economyModule.getStockEntry(template, regionNode);
    }

    /*
     * ===========
     * Mount / Pets
     * ===========
     */

    @Override
    public void mount(UOPlayer player, UONpc npc) {
        mobileModule.mount(player, npc);
    }

    @Override
    public void unmount(UOPlayer player) {
        mobileModule.unmount(player);
    }

    /*
     * =========
     * Scheduling
     * =========
     */

    @Override
    public void scheduleTask(GameTask task) {
        gameLoop.addTask(task);
    }

    /*
     * ==========
     * Utilities
     * ==========
     */

    @Override
    public boolean roll(double chance) {
        return rng.roll(chance);
    }

    @Override
    public CompletableFuture<UOAccount> getAccountByUsername(String username) {
        return storage.getAccountByUsername(username);
    }

    /*
     * ==========
     * AI
     * ==========
     */

    @Override
    public void detachAI(UONpc npc) {
        aiModule.detach(npc);
    }
}