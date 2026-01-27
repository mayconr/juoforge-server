package com.github.mayconr.juoserver.game.session.player;

import com.github.mayconr.juoserver.ServerProperties;
import com.github.mayconr.juoserver.common.policy.PolicyService;
import com.github.mayconr.juoserver.common.policy.actions.DoubleClickPolicy;
import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.game.session.player.movement.MovementService;
import com.github.mayconr.juoserver.game.session.player.speech.SpeechService;
import com.github.mayconr.juoserver.game.session.player.target.TargetResult;
import com.github.mayconr.juoserver.game.session.player.target.TargetService;
import com.github.mayconr.juoserver.game.session.player.vitals.VitalsService;
import com.github.mayconr.juoserver.game.session.world.WorldSession;
import com.github.mayconr.juoserver.network.packet.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.function.Consumer;

@Slf4j
@RequiredArgsConstructor
public class DefaultPlayerSession implements PlayerSession {

    private final UOPlayer player;
    private final ServerProperties properties;
    private final InitializationService initializationService;
    private final PolicyService policyService;
    private final SpeechService speechService;
    private final MovementService movementService;
    private final ItemInteractionService itemInteractionService;
    private final DoubleClickService doubleClickService;
    private final MegaClilocService megaClilocService;
    private final TargetService targetService;
    private final CombatService combatService;
    private final MountService mountService;
    private final VitalsService vitalsService;


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
        final var result = policyService.evaluate(DoubleClickPolicy.class, new DoubleClickPolicy(player, doubleClick.getSerialId()));
        if (result.allowed()) {
            doubleClickService.handleDoubleClick(doubleClick);
        }
    }

    @Override
    public void equipItem(EquipItemRequest equipItem) {
        worldSession.findItemBySerialId(equipItem.getItemSerialId())
            .thenAccept(item->itemInteractionService.handleEquipItem(item, equipItem.getLayer()))
            .whenComplete(this::logging);
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

    private <T> void logging(T data, Throwable throwable) {
        if (throwable != null) {
            log.error("Unable to l");
        }
    }
}
