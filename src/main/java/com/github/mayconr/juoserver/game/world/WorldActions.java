package com.github.mayconr.juoserver.game.world;

import com.github.mayconr.juoserver.game.economy.stock.StockEntry;
import com.github.mayconr.juoserver.game.interaction.target.TargetResult;
import com.github.mayconr.juoserver.game.item.ItemCreationRequest;
import com.github.mayconr.juoserver.game.model.AnimationOptions;
import com.github.mayconr.juoserver.game.model.ConsumeResult;
import com.github.mayconr.juoserver.game.model.Container;
import com.github.mayconr.juoserver.game.model.CursorType;
import com.github.mayconr.juoserver.game.model.Direction;
import com.github.mayconr.juoserver.game.model.ItemOptions;
import com.github.mayconr.juoserver.game.model.Location;
import com.github.mayconr.juoserver.game.model.SkillGainContext;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.model.UONpc;
import com.github.mayconr.juoserver.game.model.UOObject;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.model.event.message.MessageContent;
import com.github.mayconr.juoserver.game.ui.gump.DeclarativeGumpUI;
import com.github.mayconr.juoserver.game.ui.gump.GumpHandler;
import com.github.mayconr.juoserver.infrastructure.gameloop.GameTask;

import java.util.List;
import java.util.function.Consumer;

public interface WorldActions {

    // =========================
    // Entity creation / removal
    // =========================

    UONpc createNpc(String name, Location location);

    UOItem createItem(ItemCreationRequest request, ItemOptions options);

    void deleteMobile(UOMobile mobile);

    void deleteItem(int serial);

    void deleteItem(UOItem item);

    // =========================
    // Movement / positioning
    // =========================

    void move(UOMobile mobile, Direction direction);

    void teleport(UOMobile mobile, Location location);

    void moveItem(UOItem item, Location location);

    void mount(UOPlayer player, UONpc npc);

    void unmount(UOPlayer player);

    // =========================
    // Messaging / UI
    // =========================

    void sendMessage(UOPlayer player, MessageContent content);

    void printTextAbove(UOObject source, MessageContent content);

    void printTextAbove(UOObject source, MessageContent content, UOPlayer player);

    void broadcast(MessageContent message);

    void sendAnimation(UOMobile mobile, AnimationOptions options);

    void sendGump(UOPlayer player, DeclarativeGumpUI gumpUI, GumpHandler handler);

    void sendTarget(UOPlayer player, CursorType type, Consumer<TargetResult> consumer);

    // =========================
    // Items / containers
    // =========================

    ConsumeResult consumeItem(Container container, String name, int amount, boolean searchNestedContainers);

    // =========================
    // Skills / progression
    // =========================

    void tryGainSkill(UOMobile mobile, int skillId, double difficulty, SkillGainContext context);

    // =========================
    // Economy / vendors
    // =========================

    void beginVendorPurchase(UOPlayer player, UOMobile vendor, List<StockEntry> items);

    // =========================
    // Scheduling
    // =========================

    void scheduleTask(GameTask task);
}