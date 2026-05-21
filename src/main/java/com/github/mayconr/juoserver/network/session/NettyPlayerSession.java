package com.github.mayconr.juoserver.network.session;

import com.github.mayconr.juoserver.game.GamePlaySettings;
import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.game.model.event.*;
import com.github.mayconr.juoserver.game.model.event.ItemStacked.StackDestination;
import com.github.mayconr.juoserver.game.model.event.message.LocalizedMessageContent;
import com.github.mayconr.juoserver.game.model.event.message.MessageSent;
import com.github.mayconr.juoserver.game.model.event.message.PlainTextMessageContent;
import com.github.mayconr.juoserver.game.player.exception.PlayerNameAlreadyExistsException;
import com.github.mayconr.juoserver.game.world.WorldInternal;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.region.RegionNode;
import com.github.mayconr.juoserver.network.packet.*;
import com.github.mayconr.juoserver.network.packet.EnableLockedClientFeatures.ClientFeatureFlag;
import com.github.mayconr.juoserver.network.session.i18n.ClientLocale;
import com.github.mayconr.juoserver.network.session.i18n.MessageLocalizer;
import com.github.mayconr.juoserver.network.session.i18n.ResourceBundleMessageLocalizer;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class NettyPlayerSession implements PlayerSession {

    private final MessageLocalizer localizer = new ResourceBundleMessageLocalizer("messages");
    private final Channel channel;
    private final ChannelGroup channelGroup;
    private final GamePlaySettings settings;
    private final EventBus eventBus;
    private final WorldInternal world;

    private SocketAddress remoteAddress;
    private String clientVersion;
    private ClientLocale locale = ClientLocale.PT_BR;

    @Getter
    private UOPlayer player;
    private UOAccount account;
    private final Map<Integer, AccountMobile> availableMobiles = new HashMap<>();
    private final Map<Integer, RegionNode> availableStartingLocations = new HashMap<>();
    private int mobileSerialId;

    private SessionState state;

    public NettyPlayerSession(Channel channel, ChannelGroup channelGroup, GamePlaySettings settings, EventBus eventBus, WorldInternal world) {
        this.channel = channel;
        this.channelGroup = channelGroup;
        this.settings = settings;
        this.eventBus = eventBus;
        this.world = world;
        this.state = SessionState.CONNECTED;
    }

    /*
     * =================
     * Session Lifecycle
     * =================
     */

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
        runInEventLoop(()-> channel.writeAndFlush(new LoginReject(reason)));
    }

    @Override
    public void activate() {
        this.player.setConnected(true);
        updateAndNotifyStatus(SessionState.ACTIVE);
    }

    @Override
    public void disconnect() {
        // session can be disconnected before player assigned
        if (player != null) {
            player.setConnected(false);
        }

        updateAndNotifyStatus(SessionState.DISCONNECTED);
    }

    /*
     * ===============================
     * Character Selection and Creation
     * ===============================
     */

    @Override
    public void authenticate(String username) {
        if (!SessionState.CONNECTED.equals(state)) {
            throw new IllegalStateException("Session is not connected. Session state is " + state);
        }

        world.getAccountByUsername(username)
                .thenApply(account->{
                    this.account = account;
                    return this.account;
                })
                .thenCompose(world::getPlayerMobiles)
                .thenAccept(mobiles->{
                    availableStartingLocations.clear();
                    availableMobiles.clear();

                    int mobileCounter = 0;
                    for (AccountMobile mobile : mobiles) {
                        availableMobiles.put(mobileCounter++, mobile);
                    }

                    final var counter = new AtomicInteger(0);
                    world.getRegionsByType(RegionType.STARTING_LOCATION).forEach(region -> availableStartingLocations.put(counter.getAndIncrement(), region));

                    updateAndNotifyStatus(SessionState.AUTHENTICATED);

                    runInEventLoop(()->{
                        int unlockedFeatures = 0;
                        for (ClientFeatureFlag flag : settings.client().unlockedFeatures()) {
                            unlockedFeatures |= flag.mask();
                        }

                        channel.write(new EnableLockedClientFeatures(unlockedFeatures, true));
                        channel.writeAndFlush(new CharacterList(
                                mobiles,
                                availableStartingLocations,
                                CharacterListFlag.ENABLE_AOS_COMMON,
                                CharacterListFlag.SAMURAI_NINJA_CLASSES,
                                CharacterListFlag.ENABLE_NPC_POPUP,
                                CharacterListFlag.ELVEN_RACE));
                    });
                }).whenComplete((unused, throwable) -> {
                    if (throwable != null) {
                        log.error("Error while trying to connect to server", throwable);
                    }
                });
    }

    @Override
    public AccountMobile selectCharacter(int index) {
        var mobile = availableMobiles.get(index);
        if (mobile == null) {
            throw new IllegalStateException("No mobile found for index: " + index);
        }
        this.mobileSerialId = mobile.serialId();

        updateAndNotifyStatus(SessionState.CHARACTER_SELECTED);
        return mobile;
    }

    @Override
    public CompletableFuture<UOPlayer> createCharacter(CreateCharacter character) {
        return world.createPlayerMobile(character, availableStartingLocations, account)
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
                    this.mobileSerialId = mobile.getSerialId();
                    this.player = mobile;
                    updateAndNotifyStatus(SessionState.ACTIVE);
                    return mobile;
                });
    }

    @Override
    public void deleteCharacter(DeleteCharacter deleteCharacter) {
        final var selectedMobile = availableMobiles.get(deleteCharacter.getSelectedSlot());
        if (selectedMobile == null) {
            throw  new IllegalStateException("Cannot delete character that is already selected");
        }
        world.deletePlayerMobile(selectedMobile.serialId())
                .thenAccept(unused -> {
                    reject(LoginReject.Reason.CHAR_DOES_NOT_EXIST);
                });
    }

    @Override
    public CompletableFuture<UOPlayer> enteringWorld() {
        return world.loadMobile(mobileSerialId)
                .thenApply(mobile -> {
                    if (!(mobile instanceof UOPlayer pl)) {
                        log.error("Mobile [{}] is not a player", mobile.getName());
                        reject(LoginReject.Reason.SYNCHRONIZATION_ERROR);
                        return null;
                    }

                    this.player = pl;

                    updateAndNotifyStatus(SessionState.ENTERING_WORLD);

                    return this.player;
                })
                .whenComplete((mobile, throwable) -> {
                    if (throwable != null) {
                        log.error("Failed to load mobile {}", mobileSerialId, throwable);
                        reject(LoginReject.Reason.SYNCHRONIZATION_ERROR);
                    }
                });
    }

    @Override
    public void resync(MoveResyncAck resyncAck) {
        world.resync(player, resyncAck);
        //runInEventLoop(()->{

            //channel.write(new MoveResyncAck(resyncAck.getSequence(), resyncAck.getNotoriety()));
            //channel.writeAndFlush(new DrawGamePlayer(player));
        //});
    }

    /*
     * =======================
     * Mobile and World Events
     * =======================
     */

    public void onMobileMoved(MobileMoved moved) {
        final UOMobile mobile = moved.mobile();

        // =========================
        // Case 1: Other mobile moved
        // =========================
        if (!player.equals(mobile)) {
            handleOtherMobileMovement(mobile);
            return;
        }

        // =========================
        // Case 2: Player moved
        // =========================
        handlePlayerMovement(moved);
    }

    private void handleOtherMobileMovement(UOMobile mobile) {
        if (!shouldReceiveUpdate(mobile)) {
            return;
        }
        runInEventLoop(() -> channel.writeAndFlush(new DrawMobile(mobile, world.getEquippedItems(mobile))));
    }

    private void handlePlayerMovement(MobileMoved moved) {
        int visibility = settings.world().visibility().range();

        var mobiles = world.getMobilesInRange(player, visibility, UOMobile::isAlive);
        var items   = world.getItemsInRange(player, visibility);

        // Acknowledge movement
        channel.write(new MoveResyncAck(moved.sequence(), player.getNotoriety()));

        // Draw nearby mobiles
        for (UOMobile mobile : mobiles) {
            if (!mobile.equals(player)) {
                channel.write(new DrawMobile(mobile, world.getEquippedItems(mobile)));
            }
        }

        // Draw nearby items
        for (UOItem item : items) {
            channel.write(new ObjectInfo(item));
        }

        // Update player itself
        if (moved.teleport()) {
            channel.write(new DrawGamePlayer(player));
        } else {
            channel.write(new UpdatePlayer(player));
        }

        channel.flush();

        // TODO:
        // - Send incremental updates instead of full redraw
        // - Track enter/leave range to avoid resending everything
    }

    public void onMobileMoveRejected(MobileMoveRejected rejected) {
        if (player.equals(rejected.mobile())) {
            runInEventLoop(()->{
                channel.write(new MoveReject(rejected.sequence(), rejected.mobile()));
                channel.writeAndFlush(new DrawGamePlayer(player));
            });
        }
    }

    public void onMobileResynced(MobileMoveResync resync) {
        if (player.equals(resync.player())) {
            runInEventLoop(()->{
                log.info("Mobile resynced");
                channel.write(new MoveResyncAck(resync.sequence(), resync.player().getNotoriety()));
                channel.writeAndFlush(new DrawGamePlayer(player));
            });
        }
    }

    public void onMobileSpeech(MobileSpeech event) {
        channel.writeAndFlush(new SendSpeech(event));
    }

    public void onEnteredLineOfSight(MobileEnteredLineOfSight event) {
        if (player.equals(event.target())) {
            var mobile = event.observer();

            channel.write(new DrawMobile(mobile, world.getEquippedItems(mobile)));
        }
    }

    public void onNpcCreated(NpcCreated event) {
        channel.writeAndFlush(new DrawMobile(event.npc(), world.getEquippedItems(event.npc())));
    }

    public void onMobileDeleted(NpcRemoved event) {
        channelGroup.writeAndFlush(new DeleteObject(event.npc()));
    }

    public void onPlayerDeleted(PlayerDeleted event) {
        runInEventLoop(()->channel.writeAndFlush(new DeleteObject(event.deletedPlayer())));
    }

    public void onAnimationSent(AnimationSent event) {
        channelGroup.writeAndFlush(new CharacterAnimation(event.mobile(), event.options().repeat(), event.options().type(), event.options().frame(), event.options().direction()));
    }

    public void onPlayerLoggedIn(PlayerLoggedIn event) {
        if (player.equals(event.player())) {
            final var visibility = settings.world().visibility().range();
            final var mobiles = world.getMobilesInRange(player, visibility, UOMobile::isAlive);
            final var items = world.getItemsInRange(player, visibility);

            channel.write(new LoginConfirm(player, 7168, 4096));
            channel.write(new SeasonalInformation(Season.Summer, true));

            for (UOMobile someone : mobiles) {
                if (!someone.equals(player)) {
                    channel.write(new DrawMobile(someone, world.getEquippedItems(someone)));
                }
            }

            for (UOItem item : items) {
                channel.write(new ObjectInfo(item));
            }

            channel.write(new SendSkill(player));
            channel.write(new DrawGamePlayer(player));
            channel.write(new DrawMobile(player, world.getEquippedItems(player)));
            channel.write(new StatusBarInfo(player));
            channel.write(new LoginComplete());
            channel.flush();

            //channelGroup.writeAndFlush(new DrawMobile(player, toEquippedItemsMap(player)));
        }
    }

    public void onPlayerLoggedOut(PlayerLoggedOut event) {
        if (player.equals(event.player())) {
            // do not send anything. User has logged off
            return;
        }
        channel.writeAndFlush(new DeleteObject(event.player()));
    }

    /*
     * ===========
     * Item Events
     * ===========
     */

    public void onItemEquipped(ItemEquipped equipped) {
        final var item = equipped.item();
        if (item.getCurrentLocation() instanceof EquippedLocation location) {
            runInEventLoop(()-> channel.writeAndFlush(new EquipItem(equipped.mobile(), item.getLayer(), equipped.item())), 20, TimeUnit.MILLISECONDS);
        }
    }

    public void onItemUnequipped(ItemUnequipped itemUnequipped) {
        if (player.equals(itemUnequipped.mobile())) {
            channelGroup.writeAndFlush(new DrawMobile(itemUnequipped.mobile(), world.getEquippedItems(itemUnequipped.mobile())));
        }
    }

    public void onItemDroppedOnTheGround(ItemDroppedOnTheGround event) {
        if (shouldReceiveUpdate(event.item())) {
            channel.writeAndFlush(new ObjectInfo(event.item()));
        }
    }

    public void onItemDroppedInContainer(ItemDroppedInContainer event) {
        final var container = event.container();

        // container has owner, must notify only this one
        if (container.getCurrentLocation() instanceof EquippedLocation(Integer ownerSerialId)) {
            if (ownerSerialId == player.getSerialId()) {
                runInEventLoop(()->channel.writeAndFlush(new AddItemToContainer(event.container(), event.item())));
            }
            return;
        }

        // Container in the ground
        if (shouldReceiveUpdate(event.container())) {
            runInEventLoop(()->channel.writeAndFlush(new AddItemToContainer(event.container(), event.item())));
        }

    }

    public void onItemStacked(ItemStacked event) {
        channelGroup.write(new DeleteObject(event.dropped()));
        if (StackDestination.GROUND.equals(event.destination())) {
            channelGroup.writeAndFlush(new ObjectInfo(event.target()));
        }
        if (StackDestination.CONTAINER.equals(event.destination())) {
            channelGroup.writeAndFlush(new AddItemToContainer(event.container(), event.target()));
        }
    }

    public void onItemCreated(GroundedItemCreated event) {
        if (shouldReceiveUpdate(event.item())) {
            runInEventLoop(()->channel.writeAndFlush(new ObjectInfo(event.item())),  20, TimeUnit.MILLISECONDS);
        }
    }

    public void onEquippedItemCreated(EquippedItemCreated event) {
        if (shouldReceiveUpdate(event.mobile())) {
            channel.writeAndFlush(new DrawMobile(event.mobile(), world.getEquippedItems(event.mobile())));
        }
    }

    public void onItemCreatedInContainer(ItemCreatedInContainer event) {
        if (event.owner() != null) {
            if (player.equals(event.owner())) {
                runInEventLoop(()->channel.writeAndFlush(new AddItemToContainer(event.container(), event.item())));
            }
            return;
        }

        if (shouldReceiveUpdate(event.container())) {
            runInEventLoop(()->channel.writeAndFlush(new AddItemToContainer(event.container(), event.item())));
        }
    }

    public void onItemDeleted(ItemDeleted event) {
        channel.writeAndFlush(new DeleteObject(event.item()));
    }

    public void onItemUpdated(ItemUpdated event) {
        final var item = event.item();
        channel.write(new ObjectRevision(event.item()));
        if (item.getCurrentLocation() instanceof ContainerLocation) {
            channel.writeAndFlush(new AddItemToContainer(event.container(), event.item()));
            return;
        }
        channel.writeAndFlush(new ObjectInfo(event.item()));
    }

    /*
     * ======================================
     * UI, Interaction and Communication Events
     * ======================================
     */

    public void onMessageSent(MessageSent event) {
        var speakerId = 0;
        var speakerName = "";
        var speakerModel = 0;
        var style = event.messageStyle();

        if (event.messageSource() != null) {
            speakerId = event.messageSource().getSerialId();
            speakerName = event.messageSource().getDisplayName();
            speakerModel = event.messageSource().getModelId();
        }

        final String text = switch (event.messageContent()) {
            case PlainTextMessageContent plain -> plain.text();
            case LocalizedMessageContent localized -> localizer.localize(localized, locale);
        };
        final var speech = new SendSpeech(style.type(), style.hue(), speakerId, speakerModel, style.font(), speakerName, text);

        if (style.type().equals(TextType.BROADCAST)) {
            channel.writeAndFlush(speech);
        } else {
            if (event.messageTarget() != null) {
                if (player.equals(event.messageTarget())) {
                    channel.writeAndFlush(speech);
                }
            } else {
                // TODO send to all in range
                channel.writeAndFlush(speech);
            }
        }
    }

    public void onTargetSent(TargetSent event) {
        if (player.equals(event.player())) {
            channel.writeAndFlush(new Target(event.id(), event.target(), event.type()));
        }
    }

    public void onPaperdollOpened(PaperdollOpened event) {
        if (player.equals(event.player())) {
            channel.writeAndFlush(new OpenPaperdoll(event.paperdoll(), event.flag()));
        }
    }

    public void onContainerOpened(ContainerOpenedEvent event) {
        if (player.equals(event.player())) {
            final var container = event.container();

            channel.write(new DrawContainer(container));
            if (!container.getContainerItems().isEmpty()) {
                var items = new ArrayList<UOItem>(container.getContainerItems().size());
                for (var item : container.getContainerItems()) {
                    world.getItemBySerialId(item).ifPresent(items::add);
                }
                channel.write(AddMultipleItemsToContainer.ofUOItem(container, items));
            }
            channel.flush();
        }
    }

    public void onStatusGumpRequested(StatusGumpRequested event) {
        if (player.equals(event.player())) {
            channel.writeAndFlush(new StatusBarInfo(event.requestedFor()));
        }
    }

    public void onTooltipRequested(TooltipRequested event) {
        if (player.equals(event.player())) {
            for (TooltipSupport object : event.objects()) {
                channel.writeAndFlush(new TooltipRequest(object));
            }
        }
    }

    public void onGumpSent(GumpSent event) {
        if (player.equals(event.player())) {
            channel.writeAndFlush(new SendGumpDialog(event.player(), event.gumpId(), 100, 100, event.builtGump().layout, event.builtGump().texts));
        }
    }

    /*
     * ================================
     * Skills, Status and Combat Events
     * ================================
     */

    public void onSkillGained(SkillGained event) {
        if (player.equals(event.mobile())) {
            channel.writeAndFlush(new SendSkill(SendSkillType.SINGLE_UPDATE, List.of(event.skill())));
        }
    }

    public void onSkillGumpRequested(SkillGumpRequested event) {
        if (player.equals(event.player())) {
            log.info("Sending skills of {}", event.skillsOf());
            channel.writeAndFlush(new SendSkill(SendSkillType.FULL_LIST_WITH_CAP, event.skillsOf().getSkills().getSkillValues()));
        } else {
            log.info("Not implemented yet");
        }

        if (log.isDebugEnabled()) {
            log.debug("Sending skill gump for [{}-{}]", player.getSerialId(), player.getName());
        }
    }

    public void onSkillLocked(SkillLocked event) {
        if (player.equals(event.mobile())) {
            channel.writeAndFlush(new SendSkill(SendSkillType.SINGLE_UPDATE, event.skills()));
        }
    }

    public void onMobileStatusChanged(MobileStatusChanged event) {
        if (player.equals(event.mobile())) {
            // abort any kind of combat
            if (CharacterStatus.WAR_MODE.equals(event.oldStatus())) {
                channel.write(new AttackCharacter(0));
            }

            channel.writeAndFlush(new RequestWarMode(event.newStatus().getWarModeType()));
            channelGroup.writeAndFlush(new UpdatePlayer(player));
        }
    }

    public void onPlayerStartAttack(PlayerStartAttack event) {
        if (player.equals(event.player())) {
            channelGroup.writeAndFlush(new UpdateMobileStatus(event.opponent().getSerialId(), player.getSerialId()), out -> !out.equals(event.player()));
        }
    }

    public void onVendorTradeSessionOpened(VendorSessionOpened event) {
        if (player.equals(event.player())) {
            final var vendor = event.vendor();
            final var sellContainerSerialId = vendor.getEquippedItems().get(Layer.SHOP_BUY_RESTOCK);
            final var sellContainer = world.getContainerBySerialId(sellContainerSerialId)
                    .orElseThrow(() -> new RuntimeException("Sell container not found for serial "+sellContainerSerialId));

            channel.write(AddMultipleItemsToContainer.ofStockItem(sellContainer, event.session().items().values()));
            channel.write(new VendorBuyList(sellContainer, event.session().items().values()));
            channel.write(new DrawContainer(vendor.getSerialId(), 0x0030));
            channel.flush();
            // TODO locate items for uocontainer
        }
    }

    public void onVitalsChanged(VitalsChanged event) {
        if (player.equals(event.mobile())) {
            channel.writeAndFlush(new StatusBarInfo(player));
        }
    }

    public void onMobileGoldChanged(MobileGoldChanged event) {
        if (player.equals(event.mobile())) {
            channel.writeAndFlush(new StatusBarInfo(player));
        }
    }

    public void onMobileDamaged(MobileDamagedEvent event) {
        if (player.equals(event.source()) || player.equals(event.target())) {
            runInEventLoop(()->{
                channel.write(new Damage(event.target(), event.totalDamage()));
                channel.writeAndFlush(new StatusBarInfo(event.target()));
            });
        }
    }

    public void onMobileDeath(MobileDeathEvent event) {
        runInEventLoop(()->{
            if (player.equals(event.target())) {
                channel.write(new StatusBarInfo(player));
                channel.write(new DrawMobile(player, world.getEquippedItems(player)));
                channel.writeAndFlush(new DeathScreen(DeathScreenType.SERVER));
            } else {
                channel.write(new DeathAction(event.target(), event.corpse().getSerialId()));
                channel.write(new ObjectInfo(event.corpse()));
                List<CorpseClothing.Entry> items = new ArrayList<>();
                var containerItems = ((Container) event.corpse()).getContainerItems();
                for (Integer itemSerial : containerItems) {
                    var item = world.getItemBySerialId(itemSerial).orElseThrow(() -> new RuntimeException("Container item could not be found"));
                    items.add(new CorpseClothing.Entry(item.getLayer(), item));
                }
                channel.writeAndFlush(new CorpseClothing(event.corpse(), items));
            }
        });
    }

    public void onMobileResurrect(MobileResurrectEvent event) {
        channel.write(new StatusBarInfo(player));
        channel.write(new DrawMobile(player, world.getEquippedItems(player)));
        channel.writeAndFlush(new DeathScreen(DeathScreenType.RESURRECT));
    }

    /*
     * ================
     * Internal Helpers
     * ================
     */

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

    private void runInEventLoop(Runnable runnable) {
        channel.eventLoop().execute(runnable);
    }

    private void runInEventLoop(Runnable runnable, long delay, TimeUnit unit) {
        channel.eventLoop().schedule(runnable, delay, unit);
    }

    private boolean shouldReceiveUpdate(Location target) {
        return GameMath.isInRange(player, target, settings.world().visibility().range());
    }
}
