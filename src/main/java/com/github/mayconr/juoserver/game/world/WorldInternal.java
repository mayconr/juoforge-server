package com.github.mayconr.juoserver.game.world;

import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.network.packet.*;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface WorldInternal extends WorldActions, WorldView, CombatInternal, UiInternal, SessionInternal {

    void initialize();

    void speech(UOPlayer player, UnicodeSpeachRequest request);

    void move(UOMobile mobile, MoveRequest moveRequest);

    CompletableFuture<UOMobile> loadMobile(int serialId);

    CompletableFuture<UOPlayer> createPlayer(CreateCharacter character, Map<Integer, UOCity> cities, UOAccount account);

    boolean isMobile(int serialId);

    void handleAction(UOPlayer player, ActionRequest request);

    void completeVendorPurchase(UOPlayer player, VendorBuyRequest vendorBuyRequest);

    void doubleClick(UOPlayer player, DoubleClick doubleClick);

    void singleClick(UOPlayer player, SingleClickRequest singleClick);

    void equipItem(UOPlayer player, EquipItemRequest equipItem);

    void unequipItem(UOPlayer player, UnequipItem pickedUpItem);

    void dropItemOnTheGround(UOPlayer player, DropItem dropItem);

    void dropItemInContainer(UOPlayer player, DropItem dropItem);

    void useSkill(UOPlayer player, int skillId);

    void sendSkillsLock(UOPlayer player, Collection<SkillValue> skills);

}
