package com.github.mayconr.juoserver.game.world;

import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.game.world.module.economy.RegionStockEntry;
import com.github.mayconr.juoserver.game.world.module.iteraction.target.TargetResult;
import com.github.mayconr.juoserver.game.world.module.ui.gump.DeclarativeGumpUI;
import com.github.mayconr.juoserver.game.world.module.ui.gump.GumpHandler;
import com.github.mayconr.juoserver.infrastructure.gameloop.GameTask;

import java.util.List;
import java.util.function.Consumer;

public interface WorldActions {

    UONpc createNpc(String name, Location location);

    void sendTarget(UOPlayer player, CursorType type, Consumer<TargetResult> consumer);

    void sendMessage(UOPlayer player, String text, MessageOptions options);

    void teleport(UOMobile mobile, Location location);

    void deleteMobile(UOMobile mobile);

    void sendAnimation(UOMobile mobile, AnimationOptions options);

    void deleteItem(int serial);

    void deleteItem(UOItem item);

    void moveItem(UOItem item, Location location);

    UOItem createContainerItem(String name, Container container);

    UOItem createItemAtLocation(String name, Location location);

    UOItem createEquippedItem(UOMobile mobile, String name);

    void scheduleTask(GameTask task);

    void tryGainSkill(UOMobile mobile, int skillId, double difficulty, SkillGainContext context);

    void mount(UOPlayer player, UONpc npc);

    void unmount(UOPlayer player);

    void sendGump(UOPlayer player, DeclarativeGumpUI gumpUI, GumpHandler handler);

    void tryGain(UOMobile mobile, int skillId, double difficulty, SkillGainContext context);

    void sendBuyGump(UOPlayer player, UOMobile vendor, List<RegionStockEntry> items);
}
