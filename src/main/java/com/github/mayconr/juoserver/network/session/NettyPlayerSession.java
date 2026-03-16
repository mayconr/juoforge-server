package com.github.mayconr.juoserver.network.session;

import com.github.mayconr.juoserver.JuoforgeConfiguration;
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
import com.github.mayconr.juoserver.network.session.i18n.ClientLocale;
import com.github.mayconr.juoserver.network.session.i18n.MessageLocalizer;
import com.github.mayconr.juoserver.network.session.i18n.ResourceBundleMessageLocalizer;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.net.SocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

@Slf4j
public class NettyPlayerSession implements PlayerSession {

    private final MessageLocalizer localizer = new ResourceBundleMessageLocalizer("messages");
    private final Channel channel;
    private final ChannelGroup channelGroup;
    private final JuoforgeConfiguration configuration;
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

    public NettyPlayerSession(Channel channel, ChannelGroup channelGroup, JuoforgeConfiguration configuration, EventBus eventBus, WorldInternal world) {
        this.channel = channel;
        this.channelGroup = channelGroup;
        this.configuration = configuration;
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
    public void authenticate(UOAccount account) {
        if (!SessionState.CONNECTED.equals(state)) {
            throw new IllegalStateException("Session is not connected. Session state is " + state);
        }

        this.account = account;
        world.getPlayerMobiles(account)
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
                    channel.write(new EnableLockedClientFeatures(configuration.settings().client().unlockedFeatures(), true));
                    channel.writeAndFlush(new CharacterList(
                            mobiles,
                            availableStartingLocations,
                            CharacterListFlag.ENABLE_AOS_COMMON,
                            CharacterListFlag.SAMURAI_NINJA_CLASSES,
                            CharacterListFlag.ENABLE_NPC_POPUP,
                            CharacterListFlag.ELVEN_RACE));
                });
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

    /*
     * =======================
     * Mobile and World Events
     * =======================
     */

    public void onMobileMoved(MobileMoved moved) {
        if (!player.equals(moved.mobile())) {
            final var mobile = moved.mobile();

            if (world.isInRange(player, mobile, configuration.settings().world().lightOfSight())) {
                channel.writeAndFlush(new DrawMobile(mobile));
            }
            return;
        }

        // handle player movement
        var mobiles = world.getMobilesInRange(player, configuration.settings().world().lightOfSight());
        var items  = world.getItemsInRange(player, configuration.settings().world().lightOfSight());

        channel.write(new MovementAck(moved.sequence(), player.getNotoriety()));

        for (UOMobile mobile : mobiles) {
            if (!mobile.equals(player)) {
                channel.write(new DrawMobile(mobile));
            }
        }
        for (UOItem item : items) {
            channel.write(new ObjectInfo(item));
        }

        if (moved.teleport()) {
            channel.write(new DrawGamePlayer(player));
            channelGroup.write(new UpdatePlayer(player));
        } else {
            // Notify everyone close
            channelGroup.write(new UpdatePlayer(player));
        }
        channelGroup.flush();


        // TODO drawMobile when enter on range, after that update player
    }

    public void onMobileSpeech(MobileSpeech event) {
        channel.writeAndFlush(new SendSpeech(event));
    }

    public void onEnteredLineOfSight(MobileEnteredLineOfSight event) {
        if (player.equals(event.target())) {
            channel.write(new DrawMobile(event.observer()));
        }
    }

    public void onNpcCreated(NpcCreated event) {
        channel.writeAndFlush(new DrawMobile(event.npc()));
    }

    public void onMobileDeleted(MobileDeleted event) {
        channelGroup.writeAndFlush(new DeleteObject(event.mobile()));
    }

    public void onPlayerDeleted(PlayerDeleted event) {
        runInEventLoop(()->channel.writeAndFlush(new DeleteObject(event.deletedPlayer())));
    }

    public void onAnimationSent(AnimationSent event) {
        channelGroup.writeAndFlush(new CharacterAnimation(event.mobile(), event.options().repeat(), event.options().type(), event.options().frame(), event.options().direction()));
    }

    public void onPlayerLoggedIn(PlayerLoggedIn event) {
        if (player.equals(event.player())) {
            final var mobiles = world.getMobilesInRange(player, configuration.settings().world().lightOfSight());
            final var items = world.getItemsInRange(player, configuration.settings().world().lightOfSight());

            channel.write(new LoginConfirm(player, 7168, 4096));
            channel.write(new SeasonalInformation(Season.Summer, true));

            for (UOMobile someone : mobiles) {
                if (!someone.equals(player)) {
                    channel.write(new DrawMobile(someone));
                }
            }

            for (UOItem item : items) {
                channel.write(new ObjectInfo(item));
            }

            channel.write(new SendSkill(player));
            channel.write(new DrawGamePlayer(player));
            channel.write(new DrawMobile(player));
            channel.write(new StatusBarInfo(player));
            channel.write(new LoginComplete());
            channel.flush();

            channelGroup.writeAndFlush(new DrawMobile(player));
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
        runInEventLoop(()-> channel.writeAndFlush(new EquipItem(equipped.mobile(), equipped.item().getLayer(), equipped.item())), 20, TimeUnit.MILLISECONDS);
    }

    public void onItemUnequipped(ItemUnequipped itemUnequipped) {
        if (player.equals(itemUnequipped.player())) {
            channelGroup.writeAndFlush(new DrawMobile(itemUnequipped.player()));
        }
    }

    public void onItemDroppedOnTheGround(ItemDroppedOnTheGround event) {
        channelGroup.writeAndFlush(new ObjectInfo(event.item()));
    }

    public void onItemDroppedInContainer(ItemDroppedInContainer event) {
        if (player.equals(event.player())) {
            channel.writeAndFlush(new AddItemToContainer(event.container(), event.item()));
        }
    }

    public void onItemStacked(ItemStacked event) {
        channelGroup.write(new DeleteObject(event.dropped()));
        if (StackDestination.GROUND.equals(event.destination())) {
            channelGroup.writeAndFlush(new ObjectInfo(event.target()));
        }
        if (StackDestination.CONTAINER.equals(event.destination())) {
            channelGroup.writeAndFlush(new AddItemToContainer(event.target().getContainer(), event.target()));
        }
    }

    public void onItemCreated(GroundedItemCreated event) {
        runInEventLoop(()->channel.writeAndFlush(new ObjectInfo(event.item())),  20, TimeUnit.MILLISECONDS);
    }

    public void onItemCreated(EquippedItemCreated event) {
        channelGroup.writeAndFlush(new DrawMobile(event.mobile()));
    }

    public void onItemCreated(ItemCreatedInContainer event) {
        if (event.container() instanceof UOPlayer pl && pl.equals(player)) {
            channel.writeAndFlush(new AddItemToContainer(pl, event.item()));
            return;
        }

        channelGroup.writeAndFlush(new AddItemToContainer(event.container(), event.item()));
    }

    public void onItemDeleted(ItemDeleted event) {
        channel.writeAndFlush(new DeleteObject(event.item()));
    }

    public void onItemUpdated(ItemUpdated event) {
        channel.write(new ObjectRevision(event.item()));
        if (event.item().isInContainer()) {
            channel.writeAndFlush(new AddItemToContainer(event.item().getContainer(), event.item()));
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

    public void onContainerOpened(ContainerOpened event) {
        if (player.equals(event.player())) {
            final var container = event.container();

            channel.write(new DrawContainer(container));
            if (!container.getItemsInContainer().isEmpty()) {
                channel.write(AddMultipleItemsToContainer.ofUOItem(container, container.getItemsInContainer()));
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
            channel.writeAndFlush(new SendSkill(SendSkillType.FULL_LIST_WITH_CAP, event.skillsOf().getSkills().skills()));
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
            final var restockContainer = (UOContainer) vendor.getEquippedItems().get(Layer.SHOP_BUY_RESTOCK);
            channel.write(AddMultipleItemsToContainer.ofStockItem(restockContainer, event.session().items().values()));
            channel.write(new VendorBuyList(restockContainer, event.session().items().values()));
            channel.write(new DrawContainer(vendor.getSerialId(), 0x0030));
            channel.flush();
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

    private Predicate<UOPlayer> lineOfSightMobilesFilter() {
        return mobile->world.getMobilesInRange(player, configuration.settings().world().lightOfSight()).contains(mobile);
    }

    private void runInEventLoop(Runnable runnable) {
        channel.eventLoop().execute(runnable);
    }

    private void runInEventLoop(Runnable runnable, long delay, TimeUnit unit) {
        channel.eventLoop().schedule(runnable, delay, unit);
    }
}
