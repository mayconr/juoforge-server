package com.github.mayconr.juoserver.game.session.world;

import com.github.mayconr.juoserver.game.gameloop.GameLoop;
import com.github.mayconr.juoserver.game.gameloop.GameTask;
import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.game.reader.UOFileReader;
import com.github.mayconr.juoserver.game.session.SessionFanout;
import com.github.mayconr.juoserver.game.session.SessionOutbound;
import com.github.mayconr.juoserver.game.session.npc.NpcSession;
import com.github.mayconr.juoserver.game.session.player.PlayerSession;
import com.github.mayconr.juoserver.game.session.player.PlayerSessionFactory;
import com.github.mayconr.juoserver.game.session.player.target.TargetResult;
import com.github.mayconr.juoserver.game.session.world.animation.AnimationService;
import com.github.mayconr.juoserver.game.session.world.item.EquipItemService;
import com.github.mayconr.juoserver.game.session.world.item.ItemService;
import com.github.mayconr.juoserver.game.session.world.movement.MovementService;
import com.github.mayconr.juoserver.game.session.world.npc.NpcService;
import com.github.mayconr.juoserver.game.session.world.player.PlayerCreationService;
import com.github.mayconr.juoserver.game.session.world.player.PlayerRemovalService;
import com.github.mayconr.juoserver.game.session.world.player.PlayerSessionService;
import com.github.mayconr.juoserver.game.session.world.skill.SkillService;
import com.github.mayconr.juoserver.game.session.world.speech.SpeechService;
import com.github.mayconr.juoserver.game.session.world.status.StatusService;
import com.github.mayconr.juoserver.game.skill.SkillSystem;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;
import com.github.mayconr.juoserver.network.handler.AttributeKeys;
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
    private final SessionFanout fanout;
    private final GameLoop gameLoop;
    private final SkillSystem skillSystem;

    // Session Factories
    private final PlayerSessionFactory playerSessionFactory;

    // Services
    private final MessageService messageService;
    private final ItemService itemService;
    private final PlayerCreationService playerCreationService;
    private final PlayerSessionService playerSessionService;
    private final UOFileReader fileReader;
    private final AnimationService animationService;
    private final NpcService npcService;
    private final MovementService movementService;
    private final SpeechService speechService;
    private final EquipItemService equipItemService;
    private final PlayerRemovalService playerRemovalService;
    private final SkillService skillService;
    private final StatusService statusService;


    private final Map<UONpc, NpcSession> sessionMap = new HashMap<>();

    @Override
    public void initialize() {
        storage.initialize(serialGenerator::getCurrentItemSerial, serialGenerator::getCurrentMobileSerial);
        serialGenerator.initialize();
        playerSessionFactory.initialize(this);
        fileReader.loadFiles();
        skillSystem.initialize(this);
        npcService.initialize(this);
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
    public CompletableFuture<PlayerSession> createAndLoginPlayer(CreateCharacter character, SessionOutbound outbound) {
        return playerCreationService.createNewPlayer(character, outbound)
                .thenCompose(player -> playerSessionService.create(player, outbound))
                .thenApply(session -> registerSessionAttribute(outbound, session))
                .exceptionally(throwable -> {
                    log.error("Error creating player", throwable);
                    outbound.writeAndFlush(new LoginReject(LoginReject.Reason.SYNCHRONIZATION_ERROR));
                    return null;
                });
    }

    @Override
    public CompletableFuture<PlayerSession> loginExistingPlayer(UOPlayer player, SessionOutbound outbound) {
        return playerSessionService.create(player, outbound)
                .thenApply(session -> registerSessionAttribute(outbound, session));
    }

    private PlayerSession registerSessionAttribute(SessionOutbound outbound, PlayerSession session) {
        outbound.attr().set(AttributeKeys.PLAYER_SESSION_KEY, session);
        outbound.writeAndFlush(new ClientVersion());
        return session;
    }

    @Override
    public PlayerSession getPlayerSession(UOMobile mobile) {
        if (mobile instanceof UOPlayer player) {
            return playerSessionService.getSession(player);
        }
        throw new IllegalArgumentException("Mobile is not a player");
    }

    @Override
    public List<UOMobile> getMobilesInRange(Location location, int radius) {
        return storage.getMobilesInRange(location, radius);
    }

    @Override
    public List<UOMobile> getOtherMobilesInRange(UOObject object, int radius) {
        var candidates = storage.getMobilesInRange(object, radius);

        if (candidates.isEmpty()) {
            return candidates;
        }

        List<UOMobile> result = new ArrayList<>(candidates.size());

        int selfSerial = object.getSerialId();

        for (UOMobile mobile : candidates) {
            if (mobile.getSerialId() == selfSerial) continue;
            result.add(mobile);
        }

        return result;
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
    public void dropItemOnTheGround(UOItem item) {
        storage.dropItemOnTheGround(item);
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
    public List<UOItem> itemsInRange(Location location, int range) {
        return List.of();
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
        return itemService.createItemAtLocation(name, location);
    }

    @Override
    public void sendAnimation(UOMobile mobile, AnimationOptions options) {
        animationService.sendAnimation(mobile, options);
    }

    @Override
    public void move(UOMobile mobile, Direction dir) {
        if (mobile instanceof UOPlayer player) {
            // TODO playerSessionService.getSession(player).move();
        }
    }

    @Override
    public void deleteItem(int serial) {
        if (!UOItem.isItem(serial)) {
            throw new IllegalArgumentException("Serial ["+serial+"] is not an item");
        }
        final var item = storage.getItemBySerialId(serial).orElseThrow(()->new IllegalArgumentException("Item ["+serial+"] not found"));
        itemService.deleteItem(item);
    }

    @Override
    public void deleteItem(UOItem item) {
        itemService.deleteItem(item);
    }

    @Override
    public void moveItem(UOItem item, Location location) {
        itemService.moveItem(item, location);
    }

    @Override
    public UOItem createItemInContainer(String name, Container container) {
        return itemService.createItemInContainer(name, container);
    }

    @Override
    public void sendTarget(UOPlayer player, CursorType type, Consumer<TargetResult> consumer) {
        playerSessionService.getSession(player).sendTarget(type, consumer);
    }

    @Override
    public void sendMessage(UOPlayer player, String text, MessageOptions options) {
        playerSessionService.getSession(player).sendMessage(text, options);
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
                .ifPresent(item->equipItemService.equipItem(player, item));
    }

    @Override
    public void unequipItem(UOPlayer player, UnequipItem pickedUpItem) {
        equipItemService.unequipItem(player, pickedUpItem);
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
}
