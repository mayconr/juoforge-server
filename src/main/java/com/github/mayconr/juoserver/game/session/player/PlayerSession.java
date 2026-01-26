package com.github.mayconr.juoserver.game.session.player;

import java.util.List;
import java.util.function.Consumer;

import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.game.session.player.target.TargetResult;
import com.github.mayconr.juoserver.game.session.world.WorldSession;
import com.github.mayconr.juoserver.network.packet.*;

public interface PlayerSession {

    UOMobile getPlayer();

    boolean isActive();

    void initialize(WorldSession worldSession, String clientVersion);

    void speech(UnicodeSpeachRequest request);

    void move(MoveRequest moveRequest);

    void showMegaCliloc(List<Integer> serialList);

    void pickUpItem(PickUpItem pickedUpItem);

    void dropItemOnTheGround(DropItem droppedItem);

    void dropItemInContainer(DropItem droppedItem);

    void doubleClick(DoubleClick doubleClick);

    void move(Location location);

    void equipItem(EquipItemRequest equipItem);

    void openContainerInRange(Container container);

    void sendTarget(CursorType type, Consumer<TargetResult> consumer);

    void handleTarget(Target target);

    void handleWarMode(WarModeType type);

    void attack(int opponentSerialId);

    void mount(String mount);

    void unmount();
}
