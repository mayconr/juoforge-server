package com.github.mayconr.juoserver.game.session.player;

import com.github.mayconr.juoserver.common.policy.ActionPolicyService;
import com.github.mayconr.juoserver.common.policy.DefaultActionContext;
import com.github.mayconr.juoserver.common.policy.PolicyActions;
import com.github.mayconr.juoserver.common.policy.PolicyResult;
import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.game.session.player.speech.SpeechService;
import com.github.mayconr.juoserver.game.session.player.target.TargetResult;
import com.github.mayconr.juoserver.game.session.player.target.TargetService;
import com.github.mayconr.juoserver.game.session.world.WorldSession;
import com.github.mayconr.juoserver.network.packet.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Clock;
import java.util.List;
import java.util.function.Consumer;

@Slf4j
@RequiredArgsConstructor
public class DefaultPlayerSession implements PlayerSession {

    private final UOPlayer player;
    private final InitializationService initializationService;
    private final ActionPolicyService policyService;
    private final SpeechService speechService;
    private final MovementService movementService;
    private final ItemInteractionService itemInteractionService;
    private final DoubleClickService doubleClickService;
    private final MegaClilocService megaClilocService;
    private final TargetService targetService;
    private final CombatService combatService;
    private final MountService mountService;


    private WorldSession worldSession;
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
    public void initialize(WorldSession worldSession, String clientVersion) {
        this.worldSession = worldSession;
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
        itemInteractionService.handlePickUpItem(pickedUpItem);
    }

    @Override
    public void dropItemOnTheGround(DropItem droppedItem) {
        itemInteractionService.handleDropItemOnTheGround(droppedItem);
    }

    @Override
    public void dropItemInContainer(DropItem droppedItem) {
        itemInteractionService.handleDropItemInContainer(droppedItem);
    }

    @Override
    public void doubleClick(DoubleClick doubleClick) {
        final PolicyResult result = policyService.evaluate(PolicyActions.DOUBLE_CLICK, new DefaultActionContext(player, worldSession, Clock.systemDefaultZone()));
        if (!result.allowed()) {
            log.info("Double click handler blocked by policy. Reason: {}", result.reason());
            return;
        }
        doubleClickService.handleDoubleClick(doubleClick);
    }

    @Override
    public void equipItem(EquipItemRequest equipItem) {
        itemInteractionService.handleEquipItem(equipItem);
    }

    @Override
    public void openContainerInRange(Container container) {
        itemInteractionService.handleOpenContainer(container);
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
}
