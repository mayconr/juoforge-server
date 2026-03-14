package com.github.mayconr.juoserver.game.world;

import com.github.mayconr.juoserver.JuoforgeConfiguration;
import com.github.mayconr.juoserver.game.ai.AIModule;
import com.github.mayconr.juoserver.game.ai.BehaviorProfileRegistry;
import com.github.mayconr.juoserver.game.ai.NpcAiRegistry;
import com.github.mayconr.juoserver.game.ai.session.AISessionHandler;
import com.github.mayconr.juoserver.game.combat.CombatHandler;
import com.github.mayconr.juoserver.game.combat.CombatModule;
import com.github.mayconr.juoserver.game.combat.DefaultCombatSystem;
import com.github.mayconr.juoserver.game.combat.VitalsHandler;
import com.github.mayconr.juoserver.game.economy.EconomyModule;
import com.github.mayconr.juoserver.game.economy.StockHandler;
import com.github.mayconr.juoserver.game.economy.VendorHandler;
import com.github.mayconr.juoserver.game.economy.stock.StockEntry;
import com.github.mayconr.juoserver.game.economy.template.RegionStockTemplate;
import com.github.mayconr.juoserver.game.interaction.InteractionModule;
import com.github.mayconr.juoserver.game.interaction.action.ActionHandler;
import com.github.mayconr.juoserver.game.interaction.animation.AnimationHandler;
import com.github.mayconr.juoserver.game.interaction.speech.SpeechHandler;
import com.github.mayconr.juoserver.game.interaction.target.TargetHandler;
import com.github.mayconr.juoserver.game.interaction.target.TargetResult;
import com.github.mayconr.juoserver.game.item.*;
import com.github.mayconr.juoserver.game.item.template.ItemTemplate;
import com.github.mayconr.juoserver.game.item.template.ItemTemplateRegistry;
import com.github.mayconr.juoserver.game.item.trigger.ItemUseService;
import com.github.mayconr.juoserver.game.mobile.ItemEquipHandler;
import com.github.mayconr.juoserver.game.mobile.MobileModule;
import com.github.mayconr.juoserver.game.mobile.MountHandler;
import com.github.mayconr.juoserver.game.mobile.movement.MobileMovementRules;
import com.github.mayconr.juoserver.game.mobile.movement.MovementHandler;
import com.github.mayconr.juoserver.game.mobile.movement.RangeDetection;
import com.github.mayconr.juoserver.game.mobile.npc.MobileHandler;
import com.github.mayconr.juoserver.game.mobile.npc.template.NpcTemplateRegistry;
import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.game.model.event.*;
import com.github.mayconr.juoserver.game.player.PlayerCreationHandler;
import com.github.mayconr.juoserver.game.player.PlayerModule;
import com.github.mayconr.juoserver.game.player.PlayerVitalsHandler;
import com.github.mayconr.juoserver.game.player.template.BodyKey;
import com.github.mayconr.juoserver.game.player.template.BodyTemplate;
import com.github.mayconr.juoserver.game.player.template.StartkitTemplate;
import com.github.mayconr.juoserver.game.skill.DefaultSkillSystem;
import com.github.mayconr.juoserver.game.skill.SkillHandler;
import com.github.mayconr.juoserver.game.skill.SkillModule;
import com.github.mayconr.juoserver.game.ui.*;
import com.github.mayconr.juoserver.game.ui.gump.DeclarativeGumpUI;
import com.github.mayconr.juoserver.game.ui.gump.DefaultGumpSystem;
import com.github.mayconr.juoserver.game.ui.gump.GumpHandler;
import com.github.mayconr.juoserver.infrastructure.datafile.UOFileReaderSystem;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventHandler;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventRegistry;
import com.github.mayconr.juoserver.infrastructure.eventbus.GameEvent;
import com.github.mayconr.juoserver.infrastructure.gameloop.GameLoop;
import com.github.mayconr.juoserver.infrastructure.gameloop.GameTask;
import com.github.mayconr.juoserver.infrastructure.policy.PolicyService;
import com.github.mayconr.juoserver.infrastructure.region.RegionNode;
import com.github.mayconr.juoserver.infrastructure.region.RegionSystem;
import com.github.mayconr.juoserver.infrastructure.rng.RNG;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;
import com.github.mayconr.juoserver.infrastructure.template.InMemoryTemplateRegistry;
import com.github.mayconr.juoserver.infrastructure.template.JsonTemplateLoader;
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

    // Modules
    private EconomyModule economyModule;
    private AIModule aiModule;
    private UIModule uiModule;
    private SkillModule skillModule;
    private ItemModule itemModule;
    private PlayerModule playerModule;
    private CombatModule combatModule;
    private MobileModule mobileModule;
    private InteractionModule interactionModule;

    // Systems
    private final EventBus eventBus;
    private final SerialGenerator serialGenerator;
    private final RealmStorage storage;
    private final GameLoop gameLoop;
    private final RegionSystem regionSystem;
    private final UOFileReaderSystem fileReader;
    private final PolicyService policyService;
    private final ItemUseService itemUseService;
    private final RNG rng;

    // Templates
    private final ItemTemplateRegistry itemTemplateRegistry;
    private final NpcTemplateRegistry npcTemplateRegistry;

    // Properties
    private final JuoforgeConfiguration configuration;

    public void update(double delta) {
        aiModule.update(delta);
        playerModule.update(delta);
    }

    @Override
    public void initialize() {
        serialGenerator.initialize();
        fileReader.loadFiles();

        final var wallet = configuration.world().wallet().apply(this);

        // Economy Module
        final var pricingStrategy = configuration.world().pricingStrategy().get();
        final var vendorHandler = new VendorHandler(eventBus, serialGenerator, pricingStrategy);
        final var stockHandler = new StockHandler();
        final var templateLoader = new JsonTemplateLoader<>(Path.of("template/stock"), RegionStockTemplate.class);
        this.economyModule = new EconomyModule(vendorHandler, stockHandler, wallet, templateLoader);

        // AI Module
        final var aiFactory = new NpcAiRegistry(configuration.world().aiList());
        final var profileRegistry = new BehaviorProfileRegistry(configuration.world().behaviorProfileList());
        final var aiSessionHandler = new AISessionHandler(eventBus, profileRegistry, aiFactory);
        this.aiModule = new AIModule(eventBus, aiSessionHandler);

        // UI Module
        final var tooltipHandler = new TooltipHandler(eventBus, storage);
        final var doubleClickHandler = new DoubleClickHandler(eventBus, storage, itemUseService, policyService);
        final var singleClickHandler = new SingleClickHandler(storage);
        final var skillUIHandler = new SkillUIHandler(eventBus, storage);
        final var messageHandler = new MessageHandler(eventBus);
        final var statusHandler = new StatusHandler(eventBus, storage);
        final var gumpSystem = new DefaultGumpSystem(eventBus);
        this.uiModule = new UIModule(tooltipHandler, doubleClickHandler, singleClickHandler, skillUIHandler, gumpSystem, messageHandler, statusHandler);

        // Skill Module
        final var skillSystem = new DefaultSkillSystem(configuration, rng, eventBus);
        final var skillHandler = new SkillHandler(eventBus, storage);
        this.skillModule = new SkillModule(skillHandler, skillSystem);

        // Item Module
        final var itemHandler = new ItemHandler(serialGenerator, itemTemplateRegistry, storage, eventBus);
        final var itemDropHandler = new ItemDropHandler(eventBus, storage, policyService);
        final var containerHandler = new ContainerHandler(storage, eventBus);
        this.itemModule = new ItemModule(itemHandler, itemDropHandler, containerHandler);

        // Player Module
        final var bodies = new JsonTemplateLoader<>(Path.of("template/bodies"), BodyTemplate.class).load().values();
        final var bodyTemplateRegistry = new InMemoryTemplateRegistry<>(bodies, body->new BodyKey(body.gender(), body.race()));
        final var startkits = new JsonTemplateLoader<>(Path.of("template/startkit"), StartkitTemplate.class).load().values();
        final var startkitTemplateRegistry = new InMemoryTemplateRegistry<>(startkits, StartkitTemplate::skillId);

        final var playerCreationHandler = new PlayerCreationHandler(serialGenerator, storage, configuration, policyService, bodyTemplateRegistry, startkitTemplateRegistry);
        final var vitals = new PlayerVitalsHandler(this);
        this.playerModule = new PlayerModule(playerCreationHandler, vitals, storage, eventBus);

        // Combat Module
        final var vitalsService = new VitalsHandler(eventBus, configuration);
        final var combatSystem = new DefaultCombatSystem(null);
        final var combatService = new CombatHandler(eventBus, combatSystem, storage);
        this.combatModule = new CombatModule(combatService, vitalsService);

        // Mobile Module
        final var mountHandler = new MountHandler(eventBus, storage, policyService, itemTemplateRegistry);
        final var npcHandler = new MobileHandler(serialGenerator, storage, eventBus);
        final var movementRules = new MobileMovementRules(storage);
        final var movementHandler = new MovementHandler(eventBus, movementRules);
        final var itemEquipHandler = new ItemEquipHandler(storage, eventBus);
        this.mobileModule = new MobileModule(mountHandler, npcHandler, movementHandler, itemEquipHandler, npcTemplateRegistry, wallet, eventBus);

        // Interaction Module
        final var speechHandler = new SpeechHandler(eventBus);
        final var targetHandler = new TargetHandler(eventBus);
        final var actionHandler = new ActionHandler(eventBus);
        final var animationService = new AnimationHandler(eventBus);
        this.interactionModule = new InteractionModule(speechHandler, targetHandler, actionHandler, animationService);

        // Module Initialization
        this.economyModule.initialize(itemTemplateRegistry::get);
        this.mobileModule.initialize((player, name) -> itemModule.createEquippedItem(ItemCreationRequest.byName(name).build(), player));
        this.playerModule.initialize(itemModule::createUnloadedItem);

        // Events Registration
        eventBus.register(NpcCreated.class, created->handleNpcCreated(created.npc()));
        eventBus.register(PlayerSessionStatusChanged.class, this::handleSessionStateChanged);
        eventBus.register(PlayerSessionClosed.class, this::handleSessionClosed);
        eventBus.register(MobileMoved.class, new RangeDetection(this, eventBus, configuration));
        eventBus.register(MobileDeleted.class, this::handleMobileKilled);

        // Update Mobile Gold
        eventBus.register(ItemDroppedInContainer.class, event-> mobileModule.recalculateGold(event.player()),
                event->wallet.isGold(event.item()));
        eventBus.register(ItemCreatedInContainer.class, event->mobileModule.recalculateGold(((UOItem) event.container()).getOwner()),
                event->wallet.isGold(event.item()) && event.container() instanceof UOItem item && item.isEquipped());

        // Load world data
        this.storage.initialize(serialGenerator::getCurrentItem, serialGenerator::getCurrentMobile, data->{
            for (UONpc npc : data.npcs()) {
                this.handleNpcCreated(npc);
            }
        });
    }

    private void handleNpcCreated(UONpc npc) {
        var ai = aiModule.attach(npc);
        if (ai != null) {
            ai.wakeup(this);
        }
    }

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

    private void handleMobileKilled(MobileDeleted mobileDeleted) {
        if (mobileDeleted.mobile() instanceof UONpc npc) {
            aiModule.detach(npc);
        }
    }

    @Override
    public EventBus eventBus() {
        return eventBus;
    }

    @Override
    public JuoforgeConfiguration configuration() {
        return configuration;
    }

    @Override
    public CompletableFuture<UOMobile> loadMobile(int serialId) {
        if (!UOMobile.isMobile(serialId)) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("Serial ["+serialId+"] is not a player"));
        }
        return storage.loadMobile(serialId);
    }

    @Override
    public List<UOMobile> getMobilesInRange(Location location, int radius) {
        return storage.getMobilesInRange(location, radius);
    }

    @Override
    public boolean isMobile(int serialId) {
        return UOMobile.isMobile(serialId);
    }

    @Override
    public List<UOItem> getItemsInRange(Location location, int radius) {
        return storage.getItemsInRange(location);
    }

    @Override
    public List<UOItem> getItemsInContainer(Container container, Predicate<UOItem> predicate) {
        return itemModule.getItemsInContainer(container, predicate);
    }

    @Override
    public Optional<UOMobile> getMobileBySerialId(int serial) {
        return storage.getMobileBySerialId(serial);
    }

    @Override
    public Optional<UOItem> getItemBySerialId(int serial) {
        return storage.getItemBySerialId(serial);
    }

    @Override
    public Optional<Container> getContainerBySerialId(int serial) {
        return storage.getContainerBySerialId(serial);
    }

    @Override
    public List<Static> getStatics(Location location) {
        return fileReader.getStatics(location);
    }

    @Override
    public List<Static> getStatics(int x, int y) {
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

    @Override
    public void sendAnimation(UOMobile mobile, AnimationOptions options) {
        interactionModule.sendAnimation(mobile, options);
    }

    @Override
    public void deleteItem(int serial) {
        if (!UOItem.isItem(serial)) {
            throw new IllegalArgumentException("Serial ["+serial+"] is not an item");
        }
        final var item = storage.getItemBySerialId(serial).orElseThrow(()->new IllegalArgumentException("Item ["+serial+"] not found"));
        itemModule.deleteItem(item);
    }

    @Override
    public void deleteItem(UOItem item) {
        itemModule.deleteItem(item);
    }

    @Override
    public void moveItem(UOItem item, Location location) {
        itemModule.moveItem(item, location);
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
    public void sendMessage(UOPlayer player, String text, MessageOptions options) {
        uiModule.sendMessage(player, text, options);
    }

    @Override
    public void scheduleTask(GameTask task) {
        gameLoop.addTask(task);
    }

    @Override
    public void tryGainSkill(UOMobile mobile, int skillId, double difficulty, SkillGainContext context) {
        skillModule.tryGain(mobile, skillId, difficulty, context);
    }

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
        mobileModule.move(mobile, location);
    }

    @Override
    public void speech(UOPlayer player, UnicodeSpeachRequest request) {
        interactionModule.speech(player, request);
    }

    @Override
    public void equipItem(UOPlayer player, EquipItemRequest equipItem) {
        getItemBySerialId(equipItem.getItemSerialId())
                .ifPresent(item-> mobileModule.equipItem(player, item));
    }

    @Override
    public void unequipItem(UOPlayer player, UnequipItem pickedUpItem) {
        mobileModule.unequipItem(player, pickedUpItem);
    }

    @Override
    public UONpc createNpc(String name, Location location) {
        final var template = npcTemplateRegistry.get(name);
        if (template == null) {
            throw new IllegalArgumentException("NPC template not found "+name);
        }

        final var npc = mobileModule.createNpc(template, location);
        for (String equippedItem : template.equippedItems()) {
            final var item = itemModule.createItemAtLocation(ItemCreationRequest.byName(equippedItem).build(), npc);
            if (item == null) {
                log.error("Unable to create item [{}]", equippedItem);
                continue;
            }
            mobileModule.equipItem(npc, item);
        }
        return npc;
    }

    @Override
    public boolean isInRange(Location location1, Location location2, int radius) {
        return storage.isInRange(location1, location2, radius);
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
    public void dropItemOnTheGround(UOPlayer player, DropItem dropItem) {
        itemModule.dropItemOnTheGround(player, dropItem);
    }

    @Override
    public void dropItemInContainer(UOPlayer player, DropItem dropItem) {
        itemModule.dropItemInContainer(player, dropItem);
    }

    @Override
    public CompletableFuture<UOPlayer> createPlayerMobile(CreateCharacter character, Map<Integer, RegionNode> startingLocations, UOAccount account) {
        return playerModule.createPlayerMobile(character, startingLocations, account);
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
    public void useSkill(UOPlayer player, int skillId) {
        skillModule.useSkill(player, skillId);
    }

    @Override
    public void sendSkillsLock(UOPlayer player, Collection<SkillValue> skills) {
        skillModule.sendSkillsLock(player, skills);
    }

    @Override
    public void toggleWarMode(UOPlayer player, WarModeType type) {
        combatModule.toggleWarMode(player, type);
    }

    @Override
    public void attack(UOPlayer player, AttackRequest request) {
        combatModule.attack(player, request);
    }

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
            items.add(itemModule.createItemInContainer(ItemCreationRequest.byTemplate(item.template()).build(), player.getBackpack()));
        }
        eventBus.publish(new VendorPurchaseCompleted(player, items));
    }

    @Override
    public void handleAction(UOPlayer player, ActionRequest request) {
        interactionModule.handleAction(player, request);
    }

    @Override
    public void mount(UOPlayer player, UONpc npc) {
        mobileModule.mount(player, npc);
    }

    @Override
    public void unmount(UOPlayer player) {
        mobileModule.unmount(player);
    }

    @Override
    public void regen(UOMobile mobile, double interval) {
        combatModule.regen(mobile, interval);
    }

    @Override
    public void sendGump(UOPlayer player, DeclarativeGumpUI gumpUI, GumpHandler handler) {
        uiModule.sendGump(player, gumpUI, handler);
    }

    @Override
    public void gumpResponse(UOPlayer player, GumpSelection gumpSelection) {
        uiModule.onGumpSelection(player, gumpSelection);
    }

    @Override
    public List<UOPlayer> getOnlinePlayers() {
        return Collections.emptyList();
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
    public List<ItemTemplate> getItemsTemplate(String stockType) {
        return itemTemplateRegistry.getItemTemplates(stockType);
    }

    @Override
    public void tryGain(UOMobile mobile, int skillId, double difficulty, SkillGainContext context) {
        skillModule.tryGain(mobile, skillId, difficulty, context);
    }

    @Override
    public Optional<StockEntry> getStockEntry(ItemTemplate template, RegionNode regionNode) {
        return economyModule.getStockEntry(template, regionNode);
    }

    @Override
    public UOItem createItem(ItemCreationRequest request, ItemOptions options) {
        switch (options.target()) {
            case ItemOptions.EquipTarget equipTarget -> {
                return itemModule.createEquippedItem(request, equipTarget.mobile());
            }
            case ItemOptions.WorldLocationTarget worldLocation ->  {
                return itemModule.createItemAtLocation(request, worldLocation.location());
            }
            case ItemOptions.ContainerTarget container -> {
                return itemModule.createItemInContainer(request, container.container());
            }
        }
    }

    @Override
    public ConsumeResult consumeItem(Container container, String itemName, int amount, boolean searchNestedContainers) {
        return itemModule.consumeItem(container, itemName, amount, searchNestedContainers);
    }

    @Override
    public <T extends GameEvent> void on(Class<T> type, EventHandler<T> listener) {
        eventBus.register(type, listener);
    }

    @Override
    public <T extends GameEvent> void on(Class<T> type, EventHandler<T> listener, Predicate<T> predicate) {
        eventBus.register(type, listener, predicate);
    }

    @Override
    public <T extends GameEvent> void on(EventRegistry<T> registry) {
        eventBus.register(registry);
    }

    @Override
    public boolean roll(double chance) {
        return rng.roll(chance);
    }

    @Override
    public void removeMobile(UOMobile mobile) {
        switch (mobile) {
            case UONpc npc -> mobileModule.removeNpc(npc);
            case UOPlayer player -> log.info("Remove a player is not allowed yet {}", player.getId());
            default -> throw new IllegalStateException("Unexpected value: " + mobile);
        }
    }

    @Override
    public CompletableFuture<Void> deletePlayerMobile(int serialId) {
        return playerModule.deletePlayerMobile(serialId);
    }

    @Override
    public List<RegionNode> getRegionsByType(RegionType type) {
        return regionSystem.getRegionsByType(type);
    }

    @Override
    public CompletableFuture<List<AccountMobile>> getPlayerMobiles(UOAccount uoAccount) {
        return storage.getPlayerMobiles(uoAccount);
    }
}
