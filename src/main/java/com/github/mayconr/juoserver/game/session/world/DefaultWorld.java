package com.github.mayconr.juoserver.game.session.world;

import com.github.mayconr.juoserver.common.event.EventBus;
import com.github.mayconr.juoserver.common.template.NpcTemplateRegistry;
import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.game.session.SessionFanout;
import com.github.mayconr.juoserver.game.session.SessionOutbound;
import com.github.mayconr.juoserver.game.session.npc.NpcSession;
import com.github.mayconr.juoserver.game.session.npc.NpcSessionFactory;
import com.github.mayconr.juoserver.game.session.player.PlayerSession;
import com.github.mayconr.juoserver.game.session.player.PlayerSessionFactory;
import com.github.mayconr.juoserver.game.session.player.target.TargetResult;
import com.github.mayconr.juoserver.game.session.world.animation.AnimationService;
import com.github.mayconr.juoserver.game.session.world.file.UOFileReader;
import com.github.mayconr.juoserver.game.session.world.item.ItemService;
import com.github.mayconr.juoserver.game.session.world.player.PlayerMobileService;
import com.github.mayconr.juoserver.game.session.world.player.PlayerSessionService;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;
import com.github.mayconr.juoserver.network.handler.AttributeKeys;
import com.github.mayconr.juoserver.network.packet.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@Slf4j
@RequiredArgsConstructor
public class DefaultWorld implements WorldInternal {

    private final SerialGenerator serialGenerator;
    private final RealmStorage storage;
    private final SessionFanout fanout;
    private final EventBus eventBus;
    private final PlayerSessionFactory playerSessionFactory;
    private final NpcSessionFactory npcSessionFactory;
    private final NpcTemplateRegistry npcTemplateRegistry;

    // Services
    private final MessageService messageService;
    private final ItemService itemService;
    private final PlayerMobileService playerMobileService;
    private final PlayerSessionService playerSessionService;
    private final UOFileReader fileReader;
    private final AnimationService animationService;


    private final Map<UONpc, NpcSession> sessionMap = new HashMap<>();

    @Override
    public void initialize() {
        storage.initialize(serialGenerator::getCurrentItemSerial, serialGenerator::getCurrentMobileSerial);
        serialGenerator.initialize();
        playerSessionFactory.initialize(this);
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
    public CompletableFuture<PlayerSession> createAndLoginPlayer(CreateCharacter character, SessionOutbound outbound) {
        return playerMobileService.createPlayer(character, outbound)
                .thenCompose(player -> playerSessionService.create(player, outbound))
                .thenApply(session -> registerSessionAttribute(outbound, session))
                .exceptionally(throwable -> {
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
    public CompletableFuture<List<UOMobile>> getMobilesInRange(Location location) {
        return storage.getMobilesInRange(location);
    }

    @Override
    public MovementResult tryMove(UOMobile mobile, MoveRequest request) {
        final var direction = request.getDirection();
        Location to;
        if (mobile.getDirection().equals(direction)) {
            to = new PointInTheWorld(mobile.getX() + direction.getDx(), mobile.getY() + direction.getDy(), mobile.getZ());
        } else {
            to = mobile;
        }
        return MovementResult.success(mobile, direction, to, request.isRunning());
    }

    @Override
    public MovementResult tryMove(UOMobile mobile, Location location) {
        final int dx = location.getX() - mobile.getX();
        final int dy = location.getY() - mobile.getY();

        if (dx == 0 && dy == 0) {
            return MovementResult.denied(mobile, MovementFailureReason.BLOCKED);
        }

        final Direction direction = Direction.fromDelta(dx, dy);

        return MovementResult.success(
                mobile,
                direction,
                location,
                false
        );
    }

    @Override
    public void applyMove(UOMobile mobile, MovementResult result) {
        if (result.success()) {
            synchronized (this) {
                mobile.setDirection(result.direction());
                mobile.setRunning(result.running());
                mobile.setLocation(result.to());
                storage.updateMobileLocation(mobile, result.from(), result.to());
            }
        }
    }

    @Override
    public boolean isMobile(int serialId) {
        return UOMobile.isMobile(serialId);
    }

    @Override
    public void deleteMobile(int serialId) {
        if (!isMobile(serialId)) {
            throw new IllegalArgumentException("Serial ["+serialId+"] is not a player");
        }
        final var mobile = storage.getMobileBySerialId(serialId)
                .orElseThrow(()->new IllegalArgumentException("Mobile ["+serialId+"] not found"));

        this.deleteMobile(mobile);
    }

    @Override
    public void deleteMobile(UOMobile mobile) {
        storage.deleteMobile(mobile);
        fanout.writeAndFlush(new DeleteObject(mobile));
    }

    @Override
    public CompletableFuture<List<UOItem>> loadContainerItems(Container container) {
        return storage.loadContainerItems(container);
    }

    @Override
    public CompletableFuture<List<UOItem>> getItemsInRange(Location location) {
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

    // --- WORLD VIEW ---
    @Override
    public Optional<UOMobile> getMobileBySerialId(int serial) {
        return storage.getMobileBySerialId(serial);
    }

    @Override
    public List<UOMobile> mobilesInRange(Location location, int range) {
        return List.of();
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

    // -- WORLD ACTIONS ---

    @Override
    public void sendBroadcastMessage(String message) {
        messageService.handleSendBreadcastMessage(message);
    }

    @Override
    public UONpc createNpc(String name, Location location) {
        final var template = npcTemplateRegistry.get(name);
        if (template == null) {
            throw new IllegalArgumentException("NPC template not found "+name);
        }
        final var npc = MobileFactory.createNpcFromTemplate(serialGenerator, template, location);

        storage.cacheNpc(npc);
        fanout.writeAndFlush(new DrawMobile(npc));

        return npc;
    }

    @Override
    public UOItem createItemAtLocation(String name, Location location) {
        return itemService.createItemAtLocation(name, location);
    }

    @Override
    public void sendAnimation(UOMobile mobile, AnimationOptions options) {
        animationService.sendAnimation(mobile, options);
    }

    // -- MOBILE ACTIONS --

    @Override
    public void move(UOMobile mobile, Direction dir) {
        if (mobile instanceof UOPlayer player) {
            // TODO playerSessionService.getSession(player).move();
        }
    }

    @Override
    public void teleport(UOMobile mobile, Location location) {
        if (mobile instanceof UOPlayer player) {
            playerSessionService.getSession(player).move(location);
        }
    }

    // -- ITEMS ACTIONS --

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


    // -- PLAYER ACTIONS --

    @Override
    public void sendTarget(UOPlayer player, CursorType type, Consumer<TargetResult> consumer) {
        playerSessionService.getSession(player).sendTarget(type, consumer);
    }

    @Override
    public void sendMessage(UOPlayer player, String text, MessageOptions options) {
        playerSessionService.getSession(player).sendMessage(text, options);
    }

    @Override
    public void sendSkill(UOMobile mobile, SkillValue value) {
        if (mobile instanceof UOPlayer player) {
            playerSessionService.getSession(player).sendSkill(value);
        }
    }
}
