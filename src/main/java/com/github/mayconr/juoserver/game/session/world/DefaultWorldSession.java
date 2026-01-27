package com.github.mayconr.juoserver.game.session.world;

import com.github.mayconr.juoserver.common.event.EventBus;
import com.github.mayconr.juoserver.common.event.NpcSessionCreated;
import com.github.mayconr.juoserver.common.template.NpcTemplateRegistry;
import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.game.session.SessionFanout;
import com.github.mayconr.juoserver.game.session.SessionOutbound;
import com.github.mayconr.juoserver.game.session.npc.NpcSession;
import com.github.mayconr.juoserver.game.session.npc.NpcSessionFactory;
import com.github.mayconr.juoserver.game.session.player.PlayerSession;
import com.github.mayconr.juoserver.game.session.player.PlayerSessionFactory;
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
import java.util.concurrent.CompletableFuture;

@Slf4j
@RequiredArgsConstructor
public class DefaultWorldSession implements WorldSession {

    private static final int MOBILES_MAX_SERIAL_ID = 0x3FFFFFFF;
    public static final int OBJECTS_MIN_SERIAL_ID = MOBILES_MAX_SERIAL_ID + 1;
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

    private final Map<UONpc, NpcSession> npcNpcSessionMap = new HashMap<>();

    @Override
    public void initialize() {
        storage.initialize(serialGenerator::getCurrentItemSerial, serialGenerator::getCurrentMobileSerial);
        serialGenerator.initialize();
        playerSessionFactory.initialize(this);
    }

    @Override
    public void sendBroadcastMessage(String message) {
        messageService.handleSendBreadcastMessage(message);
    }

    /*
        MOBILE METHODS
     */

    @Override
    public CompletableFuture<UOMobile> findMobileBySerialId(int serialId) {
        return storage.findMobileBySerialId(serialId);
    }

    @Override
    public CompletableFuture<PlayerSession> createAndLoginPlayer(UOAccount account, CreateCharacter character, SessionOutbound outbound) {
        return playerMobileService.createPlayer(account, character, outbound)
                .thenCompose(player -> playerSessionService.create(player, outbound))
                .thenApply(session -> registerSessionAttribute(outbound, session))
                .whenComplete((unused, throwable) -> {
                    if (throwable != null) {
                        log.error("Unable to create / login mobile", throwable);
                        outbound.writeAndFlush(new LoginReject(LoginReject.Reason.SYNCHRONIZATION_ERROR));
                    }
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
        return serialId <= MOBILES_MAX_SERIAL_ID;
    }

    @Override
    public void deleteMobile(int serialId) {
        if (!isMobile(serialId)) {
            throw new IllegalArgumentException("Serial ["+serialId+"] is not a mobile");
        }
        storage.findMobileBySerialId(serialId)
            .whenComplete((mobile, throwable) -> {
                if (throwable != null) {
                    log.error("Unable to remove mobile [{}]", serialId);
                    return;
                }

                storage.deleteMobile(mobile);
                fanout.writeAndFlush(new DeleteObject(mobile));
            });
    }

    @Override
    public void deleteMobile(UOMobile mobile) {
        storage.deleteMobile(mobile);
    }

    @Override
    public CompletableFuture<NpcSession> createNpcSession(String name, Location location) {
        final var template = npcTemplateRegistry.get(name);
        final var mobile = MobileFactory.createNpcFromTemplate(serialGenerator, template, location);
        return storage.createNpc(mobile)
            .thenApply(npc -> {
                final var session = npcNpcSessionMap.putIfAbsent(npc, npcSessionFactory.create(this, npc));
                fanout.writeAndFlush(new DrawMobile(npc));
                eventBus.publish(new NpcSessionCreated(session));
                return session;
            });
    }

    @Override
    public CompletableFuture<UONpc> createNpcAtLocation(String name, Location location) {
        final var template = npcTemplateRegistry.get(name);
        if (template == null) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("NPC template not found "+name));
        }
        final var mobile = MobileFactory.createNpcFromTemplate(serialGenerator, template, location);

        return storage.createNpc(mobile)
                .thenApply(npc->{
                    fanout.writeAndFlush(new DrawMobile(mobile));
                    return npc;
                });
    }

    /*
        ITEM METHODS
     */
    @Override
    public CompletableFuture<UOItem> findItemBySerialId(int serialId) {
        return storage.findItemBySerialId(serialId);
    }

    @Override
    public CompletableFuture<UOItem> createItemAtLocation(String name, Location location) {
        return itemService.handleCreateItemAtLocation(name, location);
    }

    @Override
    public void deleteItem(int serial) {
        storage.findItemBySerialId(serial)
            .thenAccept(this::deleteItem);
    }

    @Override
    public void deleteItem(UOItem item) {
        itemService.handleDeleteItem(item);
    }

    @Override
    public void moveItem(UOItem item, Location location) {
        itemService.handleMoveItem(item, location);
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
    public CompletableFuture<Container> findContainerBySerialId(int serialId) {
        return storage.findContainerBySerialId(serialId);
    }

    @Override
    public boolean isItem(int serialId) {
        return serialId >= OBJECTS_MIN_SERIAL_ID;
    }

    @Override
    public void dropItemOnTheGround(UOItem item) {
        storage.dropItemOnTheGround(item);
    }

    @Override
    public void removeItemFromTheGround(UOItem item) {
        storage.removeItemFromTheGround(item);
    }
}
