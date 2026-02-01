package com.github.mayconr.juoserver.game.session.player;

import com.github.mayconr.juoserver.ServerProperties;
import com.github.mayconr.juoserver.common.policy.PolicyService;
import com.github.mayconr.juoserver.common.policy.actions.DoubleClickPolicy;
import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.game.session.player.action.ActionService;
import com.github.mayconr.juoserver.game.session.player.click.ClickService;
import com.github.mayconr.juoserver.game.session.player.item.PlayerItemService;
import com.github.mayconr.juoserver.game.session.player.message.PlayerMessageService;
import com.github.mayconr.juoserver.game.session.player.movement.MovementService;
import com.github.mayconr.juoserver.game.session.player.skill.PlayerSkillService;
import com.github.mayconr.juoserver.game.session.player.speech.SpeechService;
import com.github.mayconr.juoserver.game.session.player.target.TargetResult;
import com.github.mayconr.juoserver.game.session.player.target.TargetService;
import com.github.mayconr.juoserver.game.session.player.vitals.VitalsService;
import com.github.mayconr.juoserver.game.session.world.WorldInternal;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;
import com.github.mayconr.juoserver.network.packet.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

@Slf4j
@RequiredArgsConstructor
public class DefaultPlayerSession implements PlayerSession {

    private final UOPlayer player;
    private final ServerProperties properties;
    private final RealmStorage storage;
    private final InitializationService initializationService;
    private final PolicyService policyService;
    private final SpeechService speechService;
    private final MovementService movementService;
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

    private WorldInternal worldInternal;
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
        this.worldInternal = worldInternal;
        this.clientVersion = clientVersion;
        this.initializationService.initialize(this, clientVersion);
    }

    @Override
    public void speech(UnicodeSpeachRequest request) {
        speechService.handleSpeech(request);
    }

    @Override
    public void move(MoveRequest moveRequest) {
        movementService.handleMove(moveRequest);
    }

    @Override
    public void move(Location location) {
        movementService.handleMove(location);
    }

    @Override
    public void showMegaCliloc(List<Integer> serialList) {
        megaClilocService.handleMegaCliloc(serialList);
    }

    @Override
    public void pickUpItem(PickUpItem pickedUpItem) {
        playerItemService.pickUpItem(pickedUpItem);
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
        worldInternal.getItemBySerialId(equipItem.getItemSerialId())
            .ifPresent(item-> playerItemService.equipItem(item, equipItem.getLayer()));
    }

    @Override
    public void addItemToInventory(UOItem item) {
        playerItemService.addItemToInventory(item);
    }

    @Override
    public void openContainerInRange(Container container) {
        playerItemService.openContainer(container);
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
    public void sendSkill(SkillValue value) {
        skillService.sendSkill(value);
    }
}
