package com.github.mayconr.juoserver.game.session.player;

import com.github.mayconr.juoserver.ServerProperties;
import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.game.model.event.*;
import com.github.mayconr.juoserver.game.model.policy.DoubleClickPolicy;
import com.github.mayconr.juoserver.game.policy.PolicyService;
import com.github.mayconr.juoserver.game.session.SessionFanout;
import com.github.mayconr.juoserver.game.session.SessionOutbound;
import com.github.mayconr.juoserver.game.session.player.action.ActionService;
import com.github.mayconr.juoserver.game.session.player.click.ClickService;
import com.github.mayconr.juoserver.game.session.player.item.PlayerItemService;
import com.github.mayconr.juoserver.game.session.player.message.PlayerMessageService;
import com.github.mayconr.juoserver.game.session.player.skill.PlayerSkillService;
import com.github.mayconr.juoserver.game.session.player.target.TargetResult;
import com.github.mayconr.juoserver.game.session.player.target.TargetService;
import com.github.mayconr.juoserver.game.session.player.vendor.VendorService;
import com.github.mayconr.juoserver.game.session.player.vitals.VitalsService;
import com.github.mayconr.juoserver.game.session.world.WorldInternal;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;
import com.github.mayconr.juoserver.network.packet.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

@Slf4j
@RequiredArgsConstructor
public class DefaultPlayerSession implements PlayerSession {

    private final UOPlayer player;
    private final SessionOutbound outbound;
    private final SessionFanout fanout;
    private final ServerProperties properties;
    private final RealmStorage storage;
    private final InitializationService initializationService;
    private final PolicyService policyService;
    private final PlayerItemService playerItemService;
    private final ClickService clickService;
    private final MegaClilocService megaClilocService;
    private final TargetService targetService;
    private final CombatService combatService;
    private final MountService mountService;
    private final VitalsService vitalsService;
    private final PlayerSkillService skillService;
    private final ActionService actionService;
    private final StatusService statusService;
    private final PlayerMessageService playerMessageService;
    private final VendorService vendorService;

    private WorldInternal world;
    private String clientVersion;
    private boolean active;

    public UOMobile getPlayer() {
        return player;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        player.setConnected(active);
        this.active = active;
    }

    @Override
    public void initialize(WorldInternal worldInternal, String clientVersion) {
        this.world = worldInternal;
        this.clientVersion = clientVersion;
        this.initializationService.initialize(this, clientVersion);
    }

    @Override
    public void showMegaCliloc(List<Integer> serialList) {
        megaClilocService.handleMegaCliloc(serialList);
    }

    @Override
    public void dropItemOnTheGround(DropItem droppedItem) {
        playerItemService.dropItemOnTheGround(droppedItem);
    }

    @Override
    public void dropItemInContainer(DropItem droppedItem) {
        playerItemService.dropItemInContainer(droppedItem);
    }

    @Override
    public void doubleClick(DoubleClick doubleClick) {
        final var result = policyService.evaluate(DoubleClickPolicy.class, new DoubleClickPolicy(player, doubleClick.getSerialId()));
        if (result.allowed()) {
            clickService.doubleClick(doubleClick);
        }
    }

    @Override
    public void equipItem(EquipItemRequest equipItem) {
        world.getItemBySerialId(equipItem.getItemSerialId())
            .ifPresent(item-> playerItemService.equipItem(item, equipItem.getLayer()));
    }

    @Override
    public void addItemToInventory(UOItem item) {
        playerItemService.addItemToInventory(item);
    }

    @Override
    public void sendTarget(CursorType type, Consumer<TargetResult> consumer) {
        targetService.handleSendTarget(type, consumer);
    }

    @Override
    public void handleTarget(Target target) {
        targetService.handleTarget(target);
    }

    @Override
    public void handleWarMode(WarModeType type) {
        combatService.handleWarMode(type);
    }

    @Override
    public void attack(int opponentSerialId) {
        combatService.handleAttack(opponentSerialId);
    }

    @Override
    public void mount(String mount) {
        mountService.handleMount(mount);
    }

    @Override
    public void unmount() {
        mountService.handleUnmount();
    }

    @Override
    public void useSkill(int skillId) {
        skillService.useSkill(skillId);
    }

    @Override
    public void handleAction(ActionRequest request) {
        actionService.handleAction(request);
    }

    @Override
    public void singleClick(SingleClickRequest request) {
        clickService.singleClick(request);
    }

    @Override
    public void sendSkillGump(int serialId) {
        skillService.sendGumpDialog(serialId);
    }

    @Override
    public void sendStatusGump(int serialId) {
        statusService.sendStatusGump(serialId);
    }

    @Override
    public void updateSkillsLock(Collection<SkillValue> skills) {
        skillService.updateSkillsLock(skills);
    }

    @Override
    public void sendMessage(String message, MessageOptions options) {
        playerMessageService.sendMessage(message, options);
    }

    @Override
    public void sendBuyList(UOMobile vendor, List<UOItem> items) {
        vendorService.sendBuyList(vendor, items);
    }

    public void onMobileMoved(MobileMoved moved) {
        if (player.equals(moved.mobile())) {
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
        }

        // TODO drawMobile when enter on range, after that update player
    }

    public void onMobileSpeech(MobileSpeech event) {
        if (player.equals(event.player())) {
            fanout.writeAndFlush(new SendSpeech(event), lineOfSightMobilesFilter());
        }
    }

    public void onItemEquipped(ItemEquipped equipped) {
        if (player.equals(equipped.mobile())) {
            fanout.writeAndFlush(new DrawMobile(equipped.mobile()), lineOfSightMobilesFilter());
        }
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
        fanout.writeAndFlush(new DrawMobile(event.npc()), lineOfSightMobilesFilter());
    }

    public void onNpcDeleted(NpcDeleted event) {
        fanout.writeAndFlush(new DeleteObject(event.deletedNpc()), lineOfSightMobilesFilter());
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

    private Predicate<UOPlayer> lineOfSightMobilesFilter() {
        return mobile->world.getMobilesInRange(player, properties.world().lightOfSight()).contains(mobile);
    }

}
