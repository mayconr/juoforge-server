package com.github.mayconr.juoserver.game.session.player;

import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.game.session.player.target.TargetResult;
import com.github.mayconr.juoserver.game.session.world.WorldInternal;
import com.github.mayconr.juoserver.network.packet.*;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

public interface PlayerSession {

    UOMobile getPlayer();

    boolean isActive();

    void initialize(WorldInternal worldInternal, String clientVersion);

    void showMegaCliloc(List<Integer> serialList);

    void dropItemOnTheGround(DropItem droppedItem);

    void dropItemInContainer(DropItem droppedItem);

    void doubleClick(DoubleClick doubleClick);

    void equipItem(EquipItemRequest equipItem);

    void addItemToInventory(UOItem item);

    void sendTarget(CursorType type, Consumer<TargetResult> consumer);

    void handleTarget(Target target);

    void handleWarMode(WarModeType type);

    void attack(int opponentSerialId);

    void mount(String mount);

    void unmount();

    void useSkill(int skillId);

    void handleAction(ActionRequest request);

    void singleClick(SingleClickRequest singleClickRequest);

    void sendSkillGump(int serialId);

    void sendStatusGump(int serialId);

    void updateSkillsLock(Collection<SkillValue> skills);

    void sendMessage(String message, MessageOptions options);

    void sendBuyList(UOMobile vendor, List<UOItem> items);
}
