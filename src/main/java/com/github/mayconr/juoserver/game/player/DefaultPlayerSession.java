package com.github.mayconr.juoserver.game.player;

import com.github.mayconr.juoserver.ServerProperties;
import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.game.model.event.*;
import com.github.mayconr.juoserver.game.model.event.ItemStacked.StackDestination;
import com.github.mayconr.juoserver.game.world.WorldInternal;
import com.github.mayconr.juoserver.network.packet.*;
import io.netty.channel.Channel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

@Slf4j
@RequiredArgsConstructor
public class DefaultPlayerSession implements PlayerSession {

    @Getter
    private final UOPlayer player;
    private final Channel channel;
    private final SessionOutbound outbound;
    private final SessionFanout fanout;
    private final ServerProperties properties;

    private WorldInternal world;
    private String clientVersion;

    @Override
    public SessionOutbound getOutbound() {
        return outbound;
    }

    @Override
    public void deactivate() {
        player.setConnected(false);
    }

    @Override
    public void initialize(WorldInternal world, String clientVersion) {
        this.world = world;
        this.clientVersion = clientVersion;
        this.player.setConnected(true);
        this.world.scheduleTask(new PlayerVitalsTask(player, world));
    }

    public void onMobileMoved(MobileMoved moved) {
        if (!player.equals(moved.mobile())) {
            final var mobile = moved.mobile();

            if (world.isInRange(player, mobile, properties.world().lightOfSight())) {
                outbound.writeAndFlush(new DrawMobile(mobile));
            }
            return;
        }

        // handle player movement
        var mobiles = world.getMobilesInRange(player, properties.world().lightOfSight());
        var items  = world.getItemsInRange(player, properties.world().lightOfSight());

        outbound.write(new MovementAck(moved.sequence(), player.getNotoriety()));

        for (UOMobile mobile : mobiles) {
            if (!mobile.equals(player)) {
                outbound.write(new DrawMobile(mobile));
            }
        }
        for (UOItem item : items) {
            outbound.write(new ObjectInfo(item));
        }

        if (moved.teleport()) {
            outbound.write(new DrawGamePlayer(player));
            fanout.write(new UpdatePlayer(player));
        } else {
            // Notify everyone close
            fanout.write(new UpdatePlayer(player));
        }
        fanout.flush();


        // TODO drawMobile when enter on range, after that update player
    }

    public void onMobileSpeech(MobileSpeech event) {
        channel.writeAndFlush(new SendSpeech(event));
    }

    public void onItemEquipped(ItemEquipped equipped) {
        runInEventLoop(()-> channel.writeAndFlush(new EquipItem(equipped.mobile(), equipped.item().getLayer(), equipped.item())), 20, TimeUnit.MILLISECONDS);
    }

    public void onItemUnequipped(ItemUnequipped itemUnequipped) {
        if (player.equals(itemUnequipped.player())) {
            fanout.writeAndFlush(new DrawMobile(itemUnequipped.player()), lineOfSightMobilesFilter());
        }
    }

    public void onEnteredLineOfSight(MobileEnteredLineOfSight event) {
        if (player.equals(event.target())) {
            outbound.write(new DrawMobile(event.observer()));
        }
    }

    public void onNpcCreated(NpcCreated event) {
        outbound.writeAndFlush(new DrawMobile(event.npc()));
    }

    public void onMobileDeleted(MobileDeleted event) {
        fanout.writeAndFlush(new DeleteObject(event.mobile()), lineOfSightMobilesFilter());
    }

    public void onPlayerDeleted(PlayerDeleted event) {
        fanout.writeAndFlush(new DeleteObject(event.deletedPlayer()));
    }

    public void onSkillGained(SkillGained event) {
        if (player.equals(event.mobile())) {
            outbound.writeAndFlush(new SendSkill(SendSkillType.SINGLE_UPDATE, List.of(event.skill())));
        }
    }

    public void onSkillGumpRequested(SkillGumpRequested event) {
        if (player.equals(event.player())) {
            log.info("Sending skills of {}", event.skillsOf());
            outbound.writeAndFlush(new SendSkill(SendSkillType.FULL_LIST_WITH_CAP, event.skillsOf().getSkills().skills()));
        } else {
            log.info("Not implemented yet");
        }

        if (log.isDebugEnabled()) {
            log.debug("Sending skill gump for [{}-{}]", player.getSerialId(), player.getName());
        }
    }

    public void onStatusGumpRequested(StatusGumpRequested event) {
        if (player.equals(event.player())) {
            outbound.writeAndFlush(new StatusBarInfo(event.requestedFor()));
        }
    }

    public void onTooltipRequested(TooltipRequested event) {
        if (player.equals(event.player())) {
            for (TooltipSupport object : event.objects()) {
                outbound.writeAndFlush(new TooltipRequest(object));
            }
        }
    }

    public void onItemDroppedOnTheGround(ItemDroppedOnTheGround event) {
        fanout.writeAndFlush(new ObjectInfo(event.item()), lineOfSightMobilesFilter());
    }

    public void onItemDroppedInContainer(ItemDroppedInContainer event) {
        if (player.equals(event.player())) {
            outbound.writeAndFlush(new AddItemToContainer(event.container(), event.item()));
        }
    }

    public void onItemStacked(ItemStacked event) {
        fanout.write(new DeleteObject(event.dropped()));
        if (StackDestination.GROUND.equals(event.destination())) {
            fanout.writeAndFlush(new ObjectInfo(event.target()), lineOfSightMobilesFilter());
        }
        if (StackDestination.CONTAINER.equals(event.destination())) {
            fanout.writeAndFlush(new AddItemToContainer(event.target().getContainer(), event.target()));
        }
    }

    public void onItemCreated(GroundedItemCreated event) {
        runInEventLoop(()->channel.writeAndFlush(new ObjectInfo(event.item())),  20, TimeUnit.MILLISECONDS);
    }

    public void onItemCreated(EquippedItemCreated event) {
        fanout.writeAndFlush(new DrawMobile(event.mobile()), lineOfSightMobilesFilter());
    }

    public void onItemCreated(ItemCreatedInContainer event) {
        if (event.container() instanceof UOPlayer pl && pl.equals(player)) {
            outbound.writeAndFlush(new AddItemToContainer(pl, event.item()));
            return;
        }

        fanout.writeAndFlush(new AddItemToContainer(event.container(), event.item()), lineOfSightMobilesFilter());
    }

    public void onItemDeleted(ItemDeleted event) {
        outbound.writeAndFlush(new DeleteObject(event.item()));
    }

    public void onItemUpdated(ItemUpdated event) {
        outbound.write(new ObjectRevision(event.item()));
        if (event.item().isInContainer()) {
            outbound.writeAndFlush(new AddItemToContainer(event.item().getContainer(), event.item()));
            return;
        }
        outbound.writeAndFlush(new ObjectInfo(event.item()));
    }

    public void onAnimationSent(AnimationSent event) {
        fanout.writeAndFlush(new CharacterAnimation(event.mobile(), event.options().repeat(), event.options().type(), event.options().frame(), event.options().direction()), lineOfSightMobilesFilter());
    }

    public void onPlayerLoggedIn(PlayerLoggedIn event) {
        if (player.equals(event.player())) {
            final var mobiles = world.getMobilesInRange(player, properties.world().lightOfSight());
            final var items = world.getItemsInRange(player, properties.world().lightOfSight());

            outbound.write(new LoginConfirm(player, 7168, 4096));
            outbound.write(new SeasonalInformation(Season.Summer, true));

            for (UOMobile someone : mobiles) {
                if (!someone.equals(player)) {
                    outbound.write(new DrawMobile(someone));
                }
            }

            for (UOItem item : items) {
                outbound.write(new ObjectInfo(item));
            }

            outbound.write(new SendSkill(player));
            outbound.write(new DrawGamePlayer(player));
            outbound.write(new DrawMobile(player));
            outbound.write(new StatusBarInfo(player));
            outbound.write(new LoginComplete());
            outbound.flush();

            fanout.writeAndFlush(new DrawMobile(player), lineOfSightMobilesFilter());
        }
    }

    public void onPlayerLoggedOut(PlayerLoggedOut event) {
        if (player.equals(event.player())) {
            // do not send anything. User has logged off
            return;
        }
        outbound.writeAndFlush(new DeleteObject(event.player()));
    }

    public void onMessageSent(MessageSent event) {
        if (player.equals(event.player())) {
            var speakerId = 0;
            var speakerName = "";
            var speakerModel = 0;
            var options = event.options();
            if (options.object() != null) {
                speakerId = options.object().getSerialId();
                speakerName = options.object().getDisplayName();
                speakerModel = options.object().getModelId();
            }
            outbound.writeAndFlush(new SendSpeech(options.type(), options.hue(), speakerId, speakerModel, options.font(), speakerName, event.text()));
        }
    }

    public void onTargetSent(TargetSent event) {
        if (player.equals(event.player())) {
            outbound.writeAndFlush(new Target(event.id(), event.target(), event.type()));
        }
    }

    public void onPaperdollOpened(PaperdollOpened event) {
        if (player.equals(event.player())) {
            outbound.writeAndFlush(new OpenPaperdoll(event.paperdoll(), event.flag()));
        }
    }

    public void onContainerOpened(ContainerOpened event) {
        if (player.equals(event.player())) {
            final var container = event.container();

            outbound.write(new DrawContainer(container));
            if (!container.getItemsInContainer().isEmpty()) {
                outbound.write(AddMultipleItemsToContainer.ofUOItem(container, container.getItemsInContainer()));
            }
            outbound.flush();
        }
    }

    public void onSkillLocked(SkillLocked event) {
        if (player.equals(event.mobile())) {
            outbound.writeAndFlush(new SendSkill(SendSkillType.SINGLE_UPDATE, event.skills()));
        }
    }

    public void onMobileStatusChanged(MobileStatusChanged event) {
        if (player.equals(event.mobile())) {
            // abort any kind of combat
            if (CharacterStatus.WAR_MODE.equals(event.oldStatus())) {
                outbound.write(new AttackCharacter(0));
            }

            outbound.writeAndFlush(new RequestWarMode(event.newStatus().getWarModeType()));
            fanout.writeAndFlush(new UpdatePlayer(player), lineOfSightMobilesFilter());
        }
    }

    public void onPlayerStartAttack(PlayerStartAttack event) {
        if (player.equals(event.player())) {
            fanout.writeAndFlush(new UpdateMobileStatus(event.opponent().getSerialId(), player.getSerialId()), out -> !out.equals(event.player()));
        }
    }

    public void onVendorTradeSessionOpened(VendorSessionOpened event) {
        if (player.equals(event.player())) {
            final var vendor = event.vendor();
            final var restockContainer = (UOContainer) vendor.getEquippedItems().get(Layer.SHOP_BUY_RESTOCK);
            outbound.write(AddMultipleItemsToContainer.ofStockItem(restockContainer, event.session().items().values()));
            outbound.write(new VendorBuyList(restockContainer, event.session().items().values()));
            outbound.write(new DrawContainer(vendor.getSerialId(), 0x0030));
            outbound.flush();
        }
    }

    public void onVitalsChanged(VitalsChanged event) {
        if (player.equals(event.mobile())) {
            outbound.writeAndFlush(new StatusBarInfo(player));
        }
    }

    public void onGumpSent(GumpSent event) {
        if (player.equals(event.player())) {
            outbound.writeAndFlush(new SendGumpDialog(event.player(), event.gumpId(), 100, 100, event.builtGump().layout, event.builtGump().texts));
        }
    }

    public void onMobileGoldChanged(MobileGoldChanged event) {
        if (player.equals(event.mobile())) {
            outbound.writeAndFlush(new StatusBarInfo(player));
        }
    }

    private Predicate<UOPlayer> lineOfSightMobilesFilter() {
        return mobile->world.getMobilesInRange(player, properties.world().lightOfSight()).contains(mobile);
    }

    private void runInEventLoop(Runnable runnable) {
        channel.eventLoop().execute(runnable);
    }

    private void runInEventLoop(Runnable runnable, long delay, TimeUnit unit) {
        channel.eventLoop().schedule(runnable, delay, unit);
    }
}
