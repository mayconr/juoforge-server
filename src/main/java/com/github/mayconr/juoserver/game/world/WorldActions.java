package com.github.mayconr.juoserver.game.world;

import com.github.mayconr.juoserver.game.economy.stock.StockEntry;
import com.github.mayconr.juoserver.game.model.TargetResult;
import com.github.mayconr.juoserver.game.item.ItemRequest;
import com.github.mayconr.juoserver.game.model.*;
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

    UOItem createItem(ItemRequest request, ItemTarget target);

    void deleteMobile(UOMobile mobile);

    void deleteItem(int serial);

    void deleteItem(UOItem item);

    // =========================
    // Movement / positioning
    // =========================

    void move(UOMobile mobile, Direction direction);

    void teleport(UOMobile mobile, Location location);

    void mount(UOPlayer player, UONpc npc);

    void unmount(UOPlayer player);


    // =========================
    // Messaging / UI
    // =========================

    void sendMessage(UOPlayer player, MessageContent content);

    void sendMessage(UOPlayer player, String message);

    void printTextAbove(UOObject source, MessageContent content);

    void printTextAbove(UOObject source, MessageContent content, UOPlayer player);

    void broadcast(MessageContent message);

    void sendAnimation(UOMobile mobile, AnimationOptions options);

    void sendGump(UOPlayer player, DeclarativeGumpUI gumpUI, GumpHandler handler);

    void sendTarget(UOPlayer player, CursorType type, Consumer<TargetResult> consumer);

    // =========================
    // Items / containers
    // =========================

    ConsumeResult consumeItem(Integer containerSerial, String name, int amount, boolean searchNestedContainers);

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

    // =========================
    // Vitals
    // =========================

    void applyDamage(DamageRequest request);

    void kill(UOMobile target, UOMobile source, DamageSourceKind kind);

    void resurrect(UOMobile mobile);

    // =========================
    // AI
    // =========================

    void detachAI(UONpc npc);
}