package com.github.mayconr.juoserver.game.world;

import com.github.mayconr.juoserver.game.economy.EconomySystem;
import com.github.mayconr.juoserver.game.economy.RegionStockEntry;
import com.github.mayconr.juoserver.game.economy.RegionStockPool;
import com.github.mayconr.juoserver.game.economy.StockType;
import com.github.mayconr.juoserver.game.gameloop.GameLoop;
import com.github.mayconr.juoserver.game.gameloop.GameTask;
import com.github.mayconr.juoserver.game.gump.DeclarativeGumpUI;
import com.github.mayconr.juoserver.game.gump.GumpHandler;
import com.github.mayconr.juoserver.game.gump.GumpSystem;
import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.game.reader.UOFileReader;
import com.github.mayconr.juoserver.game.region.MapRegionService;
import com.github.mayconr.juoserver.game.region.RegionNode;
import com.github.mayconr.juoserver.game.skill.SkillSystem;
import com.github.mayconr.juoserver.game.template.definitions.item.ItemTemplate;
import com.github.mayconr.juoserver.game.template.definitions.item.ItemTemplateRegistry;
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
import com.github.mayconr.juoserver.game.world.npc.NpcService;
import com.github.mayconr.juoserver.game.world.player.PlayerCreationService;
import com.github.mayconr.juoserver.game.world.player.PlayerLoginService;
import com.github.mayconr.juoserver.game.world.player.PlayerRemovalService;
import com.github.mayconr.juoserver.game.world.skill.SkillService;
import com.github.mayconr.juoserver.game.world.speech.SpeechService;
import com.github.mayconr.juoserver.game.world.status.StatusService;
import com.github.mayconr.juoserver.game.world.target.TargetResult;
import com.github.mayconr.juoserver.game.world.target.TargetService;
import com.github.mayconr.juoserver.game.world.tooltip.TooltipService;
import com.github.mayconr.juoserver.game.world.vendor.VendorService;
import com.github.mayconr.juoserver.game.world.vitals.VitalsService;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;
import com.github.mayconr.juoserver.network.packet.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@Slf4j
@RequiredArgsConstructor
public class DefaultWorld implements WorldInternal {

    // General Systems
    private final SerialGenerator serialGenerator;
    private final RealmStorage storage;
    private final GameLoop gameLoop;
    private final SkillSystem skillSystem;
    private final GumpSystem gumpSystem;
    private final MapRegionService regionService;
    private final EconomySystem economySystem;
    private final ItemTemplateRegistry itemTemplateRegistry;

    // Services
    private final MessageService messageService;
    private final ItemCreationService itemCreationService;
    private final PlayerCreationService playerCreationService;
    private final UOFileReader fileReader;
    private final AnimationService animationService;
    private final NpcService npcService;
    private final MovementService movementService;
    private final SpeechService speechService;
    private final ItemEquipService itemEquipService;
    private final PlayerRemovalService playerRemovalService;
    private final SkillService skillService;
    private final StatusService statusService;
    private final TooltipService tooltipService;
    private final ItemDropService itemDropService;
    private final PlayerLoginService playerLoginService;
    private final TargetService targetService;
    private final DoubleClickService doubleClickService;
    private final SingleClickService singleClickService;
    private final CombatService combatService;
    private final VendorService vendorService;
    private final ActionService actionService;
    private final MountService mountService;
    private final VitalsService vitalsService;

    @Override
    public void initialize() {
        storage.initialize(serialGenerator::getCurrentItemSerial, serialGenerator::getCurrentMobileSerial);
        serialGenerator.initialize();
        fileReader.loadFiles();
        skillSystem.initialize(this);
        npcService.initialize(this);
        mountService.initialize(this);
        vendorService.initialize(this);
    }

    @Override
    public CompletableFuture<UOMobile> loadMobile(int serialId) {
        if (!UOMobile.isMobile(serialId)) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("Serial ["+serialId+"] is not a player"));
        }
        return storage.loadMobile(serialId);
    }

    @Override
    public CompletableFuture<UOMobile> unloadMobile(int serialId) {
        return null;
    }

    @Override
    public CompletableFuture<UOItem> loadItem(int serialId) {
        return storage.loadItem(serialId);
    }

    @Override
    public CompletableFuture<UOItem> unloadItem(int serialId) {
        return null;
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
    public void deleteMobile(UOMobile mobile) {
        switch (mobile) {
            case UONpc npc -> npcService.deleteNpc(npc);
            case UOPlayer player -> playerRemovalService.deletePlayer(player);
            default -> throw new IllegalStateException("Unexpected value: " + mobile);
        }
    }

    @Override
    public CompletableFuture<List<UOItem>> loadContainerItems(Container container) {
        return storage.loadContainerItems(container);
    }

    @Override
    public List<UOItem> getItemsInRange(Location location, int radius) {
        return storage.getItemsInRange(location);
    }

    @Override
    public void removeItemFromTheGround(UOItem item) {
        storage.removeItemFromTheGround(item);
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
    public void sendBroadcastMessage(String message) {
        messageService.handleSendBreadcastMessage(message);
    }

    @Override
    public UOItem createItemAtLocation(String name, Location location) {
        return itemCreationService.createItemAtLocation(name, location);
    }

    @Override
    public void sendAnimation(UOMobile mobile, AnimationOptions options) {
        animationService.sendAnimation(mobile, options);
    }

    @Override
    public void deleteItem(int serial) {
        if (!UOItem.isItem(serial)) {
            throw new IllegalArgumentException("Serial ["+serial+"] is not an item");
        }
        final var item = storage.getItemBySerialId(serial).orElseThrow(()->new IllegalArgumentException("Item ["+serial+"] not found"));
        itemCreationService.deleteItem(item);
    }

    @Override
    public void deleteItem(UOItem item) {
        itemCreationService.deleteItem(item);
    }

    @Override
    public void moveItem(UOItem item, Location location) {
        itemCreationService.moveItem(item, location);
    }

    @Override
    public UOItem createContainerItem(String name, Container container) {
        return itemCreationService.createContainerItem(name, container);
    }

    @Override
    public void sendTarget(UOPlayer player, CursorType type, Consumer<TargetResult> consumer) {
        targetService.sendTarget(player, type, consumer);
    }

    @Override
    public void resolveTarget(UOPlayer player, Target target) {
        targetService.resolveTarget(player, target);
    }

    @Override
    public void sendMessage(UOPlayer player, String text, MessageOptions options) {
        messageService.sendMessage(player, text, options);
    }

    @Override
    public void scheduleTask(GameTask task) {
        gameLoop.addTask(task);
    }

    @Override
    public void tryGainSkill(UOMobile mobile, int skillId, double difficulty, SkillGainContext context) {
        skillSystem.tryGain(mobile, skillId, difficulty, context);
    }

    // REFACTORED

    @Override
    public void move(UOPlayer player, MoveRequest moveRequest) {
        movementService.move(player, moveRequest);
    }

    @Override
    public void teleport(UOMobile mobile, Location location) {
        if (mobile instanceof UOPlayer player) {
            movementService.move(player, location);
        }
    }

    @Override
    public void move(UOPlayer player, Location location) {
        movementService.move(player, location);
    }

    @Override
    public void speech(UOPlayer player, UnicodeSpeachRequest request) {
        speechService.speech(player, request);
    }

    @Override
    public void equipItem(UOPlayer player, EquipItemRequest equipItem) {
        getItemBySerialId(equipItem.getItemSerialId())
                .ifPresent(item-> itemEquipService.equipItem(player, item));
    }

    @Override
    public void unequipItem(UOPlayer player, UnequipItem pickedUpItem) {
        itemEquipService.unequipItem(player, pickedUpItem);
    }

    @Override
    public UONpc createNpc(String name, Location location) {
        return npcService.createNpc(name, location);
    }

    @Override
    public boolean isInRange(Location location1, Location location2, int radius) {
        return storage.isInRange(location1, location2, radius);
    }

    @Override
    public void skillGained(UOMobile mobile, SkillValue value) {
        skillService.skillGained(mobile, value);
    }

    @Override
    public void playerStatusRequested(UOPlayer player, GetPlayerStatus getPlayerStatus) {
        switch (getPlayerStatus.getType()) {
            case BASIC_STATUS -> statusService.sendStatusGump(player, getPlayerStatus.getSerialId());
            case REQUEST_SKILL -> skillService.sendSkillGump(player, getPlayerStatus.getSerialId());
            case GOD_CLIENT -> System.out.println("god client");
        }
    }

    @Override
    public void tooltipRequest(UOPlayer player, List<Integer> serials) {
        tooltipService.tooltipRequest(player, serials);
    }

    @Override
    public void dropItemOnTheGround(UOPlayer player, DropItem dropItem) {
        itemDropService.dropItemOnTheGround(player, dropItem);
    }

    @Override
    public void dropItemInContainer(UOPlayer player, DropItem dropItem) {
        itemDropService.dropItemInContainer(player, dropItem);
    }

    @Override
    public UOItem createEquippedItem(UOMobile mobile, String name) {
        return itemCreationService.createEquippedItem(mobile, name);
    }

    @Override
    public void login(UOPlayer player) {
        playerLoginService.login(player);
    }

    @Override
    public void logout(UOPlayer player) {
        playerLoginService.logout(player);
    }

    @Override
    public CompletableFuture<UOPlayer> createPlayer(CreateCharacter character, Map<Integer, UOCity> cities, UOAccount account) {
        return playerCreationService.createNewPlayer(character, cities, account);
    }

    @Override
    public void doubleClick(UOPlayer player, DoubleClick doubleClick) {
        doubleClickService.doubleClick(player, doubleClick);
    }

    @Override
    public void singleClick(UOPlayer player, SingleClickRequest singleClick) {
        singleClickService.singleClick(player, singleClick);
    }

    @Override
    public void useSkill(UOPlayer player, int skillId) {
        skillService.useSkill(player, skillId);
    }

    @Override
    public void sendSkillsLock(UOPlayer player, Collection<SkillValue> skills) {
        skillService.sendSkillsLock(player, skills);
    }

    @Override
    public void toggleWarMode(UOPlayer player, WarModeType type) {
        combatService.toggleWarMode(player, type);
    }

    @Override
    public void attack(UOPlayer player, AttackRequest request) {
        combatService.attack(player, request);
    }

    @Override
    public void sendBuyGump(UOPlayer player, UOMobile vendor, List<RegionStockEntry> items) {
        vendorService.sendBuyGump(player, vendor, items);
    }

    @Override
    public void handleAction(UOPlayer player, ActionRequest request) {
        actionService.handleAction(player, request);
    }

    @Override
    public void mount(UOPlayer player, UONpc npc) {
        mountService.mount(player, npc);
    }

    @Override
    public void unmount(UOPlayer player) {
        mountService.unmount(player);
    }

    @Override
    public void regen(UOMobile mobile, double interval) {
        vitalsService.regen(mobile, interval);
    }

    @Override
    public void sendGump(UOPlayer player, DeclarativeGumpUI gumpUI, GumpHandler handler) {
        gumpSystem.send(player, gumpUI, handler);
    }

    @Override
    public void gumpResponse(UOPlayer player, GumpSelection gumpSelection) {
        gumpSystem.onGumpSelection(player, gumpSelection);
    }

    @Override
    public List<UOPlayer> getOnlinePlayers() {
        return new ArrayList<>(playerLoginService.getOnlinePlayers().values());
    }

    @Override
    public void registerRegion(RegionNode region) {
        regionService.registerRegion(region);
    }

    @Override
    public Optional<RegionNode> getRegion(String name) {
        return regionService.getRegion(name);
    }

    @Override
    public Optional<RegionNode> resolveRegion(Location location) {
        return regionService.resolveRegion(location);
    }

    @Override
    public RegionStockPool getStockPool(String regionName) {
        return economySystem.getStockPool(regionName);
    }

    @Override
    public List<ItemTemplate> getItemTemplates(StockType stockType) {
        return itemTemplateRegistry.getItemTemplates(stockType);
    }

    @Override
    public double getPrice(ItemTemplate template, String regionName) {
        return economySystem.getPrice(template, regionName);
    }
}
