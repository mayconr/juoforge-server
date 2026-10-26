package com.github.mayconr.juoserver.network.session;

import com.github.mayconr.juoserver.game.GamePlaySettings;
import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.game.model.event.*;
import com.github.mayconr.juoserver.game.player.exception.PlayerNameAlreadyExistsException;
import com.github.mayconr.juoserver.game.world.WorldInternal;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.region.RegionNode;
import com.github.mayconr.juoserver.network.packet.*;
import com.github.mayconr.juoserver.network.packet.EnableLockedClientFeatures.ClientFeatureFlag;
import com.github.mayconr.juoserver.network.session.i18n.ClientLocale;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class NettyPlayerSession implements PlayerSession {

    private final SessionPacketWriter packetWriter;
    private final GamePlaySettings settings;
    private final EventBus eventBus;
    private final WorldInternal world;

    private SocketAddress remoteAddress;
    private String clientVersion;
    private ClientLocale locale = ClientLocale.PT_BR;

    @Getter
    private volatile UOPlayer player;
    private UOAccount account;
    private final Map<Integer, AccountMobile> availableMobiles = new HashMap<>();
    private final Map<Integer, RegionNode> availableStartingLocations = new HashMap<>();
    private int mobileSerialId;

    private volatile SessionState state;

    public NettyPlayerSession(Channel channel, ChannelGroup channelGroup, GamePlaySettings settings, EventBus eventBus, WorldInternal world) {
        this(new SessionPacketWriter(channel, channelGroup), settings, eventBus, world);
    }

    NettyPlayerSession(SessionPacketWriter packetWriter, GamePlaySettings settings, EventBus eventBus, WorldInternal world) {
        this.packetWriter = packetWriter;
        this.settings = settings;
        this.eventBus = eventBus;
        this.world = world;
        this.state = SessionState.CONNECTED;
    }

    synchronized boolean isActive() {
        return state == SessionState.ACTIVE && isOpenSession();
    }

    ClientLocale getLocale() {
        return locale;
    }

    @Override
    public void connect(SocketAddress remoteAddress) {
        this.remoteAddress = remoteAddress;
        updateAndNotifyStatus(SessionState.CONNECTED);
    }

    @Override
    public void setClientVersion(String version) {
        this.clientVersion = version;
    }

    @Override
    public void reject(LoginReject.Reason reason) {
        packetWriter.enqueue(packets -> packets.writeAndFlush(new LoginReject(reason)));
    }

    @Override
    public synchronized void activate() {
        requireOpenSession();
        if (state != SessionState.ENTERING_WORLD || player == null) {
            throw new IllegalStateException("Session is not entering the world");
        }
        this.player.setConnected(true);
        updateAndNotifyStatus(SessionState.ACTIVE);
    }

    @Override
    public synchronized void disconnect() {
        if (state == SessionState.DISCONNECTED) {
            return;
        }
        packetWriter.close();
        // session can be disconnected before player assigned
        if (player != null) {
            player.setConnected(false);
        }

        updateAndNotifyStatus(SessionState.DISCONNECTED);
    }

    @Override
    public synchronized void authenticate(String username) {
        requireOpenSession();
        if (!SessionState.CONNECTED.equals(state)) {
            throw new IllegalStateException("Session is not connected. Session state is " + state);
        }

        world.storage().getAccountByUsername(username)
                .thenApply(account->{
                    synchronized (this) {
                        requireOpenSession();
                        this.account = account;
                        return account;
                    }
                })
                .thenCompose(world.player()::getPlayerMobiles)
                .thenAccept(mobiles->{
                    synchronized (this) {
                        requireOpenSession();
                        availableStartingLocations.clear();
                        availableMobiles.clear();

                        int mobileCounter = 0;
                        for (AccountMobile mobile : mobiles) {
                            availableMobiles.put(mobileCounter++, mobile);
                        }

                        final var counter = new AtomicInteger(0);
                        world.map().getRegionsByType(RegionType.STARTING_LOCATION).forEach(region -> availableStartingLocations.put(counter.getAndIncrement(), region));

                        updateAndNotifyStatus(SessionState.AUTHENTICATED);

                        packetWriter.enqueue(packets -> {
                            int unlockedFeatures = 0;
                            for (ClientFeatureFlag flag : settings.client().unlockedFeatures()) {
                                unlockedFeatures |= flag.mask();
                            }

                            packets.write(new EnableLockedClientFeatures(unlockedFeatures, true));
                            packets.writeAndFlush(new CharacterList(
                                    mobiles,
                                    availableStartingLocations,
                                    CharacterListFlag.ENABLE_AOS_COMMON,
                                    CharacterListFlag.SAMURAI_NINJA_CLASSES,
                                    CharacterListFlag.ENABLE_NPC_POPUP,
                                    CharacterListFlag.ELVEN_RACE));
                        });
                    }
                }).whenComplete((unused, throwable) -> {
                    if (throwable != null) {
                        log.error("Error while trying to connect to server", throwable);
                    }
                });
    }

    @Override
    public synchronized AccountMobile selectCharacter(int index) {
        requireOpenSession();
        if (state != SessionState.AUTHENTICATED && state != SessionState.CHARACTER_SELECTED) {
            throw new IllegalStateException("Session cannot select a character in state " + state);
        }
        var mobile = availableMobiles.get(index);
        if (mobile == null) {
            throw new IllegalStateException("No mobile found for index: " + index);
        }
        this.mobileSerialId = mobile.serialId();

        updateAndNotifyStatus(SessionState.CHARACTER_SELECTED);
        return mobile;
    }

    @Override
    public synchronized CompletableFuture<UOPlayer> createCharacter(CreateCharacter character) {
        requireOpenSession();
        if (state != SessionState.AUTHENTICATED) {
            return CompletableFuture.failedFuture(new IllegalStateException("Session is not authenticated"));
        }
        return world.player().createPlayerMobile(character, availableStartingLocations, account)
                .exceptionally(throwable -> {
                    var error = unwrap(throwable);

                    if (error instanceof PlayerNameAlreadyExistsException) {
                        reject(LoginReject.Reason.CHAR_ALREADY_EXIST);
                    } else {
                        log.error("Error creating character", throwable);
                        reject(LoginReject.Reason.SYNCHRONIZATION_ERROR);
                    }
                    throw new CompletionException(throwable);
                })
                .thenApply(mobile->{
                    synchronized (this) {
                        requireOpenSession();
                        this.mobileSerialId = mobile.getSerialId();
                        updateAndNotifyStatus(SessionState.CHARACTER_SELECTED);
                        return mobile;
                    }
                });
    }

    @Override
    public void deleteCharacter(DeleteCharacter deleteCharacter) {
        final var selectedMobile = availableMobiles.get(deleteCharacter.getSelectedSlot());
        if (selectedMobile == null) {
            throw  new IllegalStateException("Cannot delete character that is already selected");
        }
        world.player().deletePlayerMobile(selectedMobile.serialId())
                .thenAccept(unused -> {
                    reject(LoginReject.Reason.CHAR_DOES_NOT_EXIST);
                });
    }

    @Override
    public synchronized CompletableFuture<UOPlayer> enteringWorld() {
        requireOpenSession();
        if (state != SessionState.CHARACTER_SELECTED) {
            return CompletableFuture.failedFuture(new IllegalStateException("No character selected"));
        }
        updateAndNotifyStatus(SessionState.ENTERING_WORLD);
        return world.player().loadMobile(mobileSerialId)
                .thenApply(mobile -> {
                    synchronized (this) {
                        if (!isOpenSession()) {
                            if (mobile instanceof UOPlayer loadedPlayer) {
                                world.player().despawn(loadedPlayer);
                            }
                            throw new CancellationException("Session closed during character loading");
                        }
                        if (!(mobile instanceof UOPlayer pl)) {
                            throw new IllegalStateException("Loaded mobile is not a player: " + mobileSerialId);
                        }
                        this.player = pl;
                        return pl;
                    }
                })
                .whenComplete((mobile, throwable) -> {
                    if (throwable != null && !(unwrap(throwable) instanceof CancellationException)) {
                        log.error("Failed to load mobile {}", mobileSerialId, throwable);
                        reject(LoginReject.Reason.SYNCHRONIZATION_ERROR);
                    }
                });
    }

    @Override
    public void resync(MoveResyncAck resyncAck) {
        world.mobile().resync(player, resyncAck);
    }

    private boolean isOpenSession() {
        return packetWriter.isActive() && state != SessionState.DISCONNECTED && state != SessionState.DISCONNECTING;
    }

    private void requireOpenSession() {
        if (!isOpenSession()) {
            throw new CancellationException("Session is closed");
        }
    }

    private void updateAndNotifyStatus(SessionState newState) {
        var oldState = state;
        this.state = newState;
        eventBus.publish(new PlayerSessionStatusChanged(this, oldState, this.state));
    }

    private Throwable unwrap(Throwable throwable) {
        Throwable current = throwable;
        while ((current instanceof CompletionException || current instanceof ExecutionException)
                && current.getCause() != null) {
            current = current.getCause();
        }
        return current;
    }

}
