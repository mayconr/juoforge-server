package com.github.mayconr.juoserver.game.world;

import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.game.world.module.ai.AIModule;
import com.github.mayconr.juoserver.game.world.module.combat.CombatModule;
import com.github.mayconr.juoserver.game.world.module.economy.EconomyModule;
import com.github.mayconr.juoserver.game.world.module.economy.RegionStockEntry;
import com.github.mayconr.juoserver.game.world.module.economy.RegionStockPool;
import com.github.mayconr.juoserver.game.world.module.economy.StockType;
import com.github.mayconr.juoserver.game.world.module.item.ItemModule;
import com.github.mayconr.juoserver.game.world.module.item.template.ItemTemplate;
import com.github.mayconr.juoserver.game.world.module.item.template.ItemTemplateRegistry;
import com.github.mayconr.juoserver.game.world.module.iteraction.InteractionModule;
import com.github.mayconr.juoserver.game.world.module.iteraction.target.TargetResult;
import com.github.mayconr.juoserver.game.world.module.mobile.MobileModule;
import com.github.mayconr.juoserver.game.world.module.player.PlayerModule;
import com.github.mayconr.juoserver.game.world.module.skill.SkillModule;
import com.github.mayconr.juoserver.game.world.module.ui.UIModule;
import com.github.mayconr.juoserver.game.world.module.ui.gump.DeclarativeGumpUI;
import com.github.mayconr.juoserver.game.world.module.ui.gump.GumpHandler;
import com.github.mayconr.juoserver.infrastructure.datafile.UOFileReaderSystem;
import com.github.mayconr.juoserver.infrastructure.gameloop.GameLoop;
import com.github.mayconr.juoserver.infrastructure.gameloop.GameTask;
import com.github.mayconr.juoserver.infrastructure.region.MapRegionSystem;
import com.github.mayconr.juoserver.infrastructure.region.RegionNode;
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

    // Modules
    private final ItemModule itemModule;
    private final PlayerModule playerModule;
    private final UIModule uiModule;
    private final CombatModule combatModule;
    private final SkillModule skillModule;
    private final EconomyModule economyModule;
    private final InteractionModule interactionModule;
    private final MobileModule mobileModule;
    private final AIModule aiModule;

    // Systems
    private final SerialGenerator serialGenerator;
    private final RealmStorage storage;
    private final GameLoop gameLoop;
    private final MapRegionSystem regionService;
    private final ItemTemplateRegistry itemTemplateRegistry;
    private final UOFileReaderSystem fileReader;

    @Override
    public void initialize() {
        storage.initialize(serialGenerator::getCurrentItemSerial, serialGenerator::getCurrentMobileSerial);
        serialGenerator.initialize();
        fileReader.loadFiles();
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
            case UONpc npc -> mobileModule.deleteNpc(npc);
            case UOPlayer player -> playerModule.deletePlayer(player);
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
    public UOItem createItemAtLocation(String name, Location location) {
        return itemModule.createItemAtLocation(name, location);
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
    public UOItem createContainerItem(String name, Container container) {
        return itemModule.createContainerItem(name, container);
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

    // REFACTORED

    @Override
    public void move(UOPlayer player, MoveRequest moveRequest) {
        interactionModule.move(player, moveRequest);
    }

    @Override
    public void teleport(UOMobile mobile, Location location) {
        if (mobile instanceof UOPlayer player) {
            interactionModule.move(player, location);
        }
    }

    @Override
    public void move(UOPlayer player, Location location) {
        interactionModule.move(player, location);
    }

    @Override
    public void speech(UOPlayer player, UnicodeSpeachRequest request) {
        interactionModule.speech(player, request);
    }

    @Override
    public void equipItem(UOPlayer player, EquipItemRequest equipItem) {
        getItemBySerialId(equipItem.getItemSerialId())
                .ifPresent(item-> itemModule.equipItem(player, item));
    }

    @Override
    public void unequipItem(UOPlayer player, UnequipItem pickedUpItem) {
        itemModule.unequipItem(player, pickedUpItem);
    }

    @Override
    public UONpc createNpc(String name, Location location) {
        return mobileModule.createNpc(name, location);
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
    public UOItem createEquippedItem(UOMobile mobile, String name) {
        return itemModule.createEquippedItem(mobile, name);
    }

    @Override
    public void login(UOPlayer player) {
        playerModule.login(player);
    }

    @Override
    public void logout(UOPlayer player) {
        playerModule.logout(player);
    }

    @Override
    public CompletableFuture<UOPlayer> createPlayer(CreateCharacter character, Map<Integer, UOCity> cities, UOAccount account) {
        return playerModule.createNewPlayer(character, cities, account);
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
    public void sendBuyGump(UOPlayer player, UOMobile vendor, List<RegionStockEntry> items) {
        var region = regionService.resolveRegion(player)
                .orElseThrow(() -> new RuntimeException("Region not found"));
        economyModule.sendBuyGump(player, vendor, region, items);
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
        return economyModule.getStockPool(regionName);
    }

    @Override
    public double getPrice(ItemTemplate template, String regionName) {
        return economyModule.getPrice(template, regionName);
    }

    @Override
    public List<ItemTemplate> getItemTemplates(StockType stockType) {
        return itemTemplateRegistry.getItemTemplates(stockType);
    }

    @Override
    public void tryGain(UOMobile mobile, int skillId, double difficulty, SkillGainContext context) {
        skillModule.tryGain(mobile, skillId, difficulty, context);
    }
}
