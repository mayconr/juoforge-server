package com.github.mayconr.juoserver.game.ui;

import com.github.mayconr.juoserver.game.model.SendSkillType;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.model.UOObject;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.model.event.ContextMenuRequested;
import com.github.mayconr.juoserver.game.ui.contextmenu.ContextMenuContext;
import com.github.mayconr.juoserver.game.ui.contextmenu.ContextMenuEntry;
import com.github.mayconr.juoserver.game.ui.contextmenu.ItemContextMenuContext;
import com.github.mayconr.juoserver.game.ui.contextmenu.MobileContextMenuContext;
import com.github.mayconr.juoserver.game.ui.flow.doubleclick.DoubleClickContext;
import com.github.mayconr.juoserver.game.ui.flow.paperdoll.OpenPaperdollContext;
import com.github.mayconr.juoserver.game.ui.flow.singleclick.SingleClickContext;
import com.github.mayconr.juoserver.game.ui.flow.skill.SkillGumpContext;
import com.github.mayconr.juoserver.game.ui.flow.status.StatusGumpContext;
import com.github.mayconr.juoserver.game.ui.flow.tooltip.TooltipContext;
import com.github.mayconr.juoserver.game.ui.gump.DeclarativeGumpUI;
import com.github.mayconr.juoserver.game.ui.gump.GumpHandler;
import com.github.mayconr.juoserver.game.ui.gump.GumpSystem;
import com.github.mayconr.juoserver.game.world.ModuleOperationException;
import com.github.mayconr.juoserver.game.world.context.ModuleContext;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.storage.WorldStorage;
import com.github.mayconr.juoserver.network.packet.DoubleClick;
import com.github.mayconr.juoserver.network.packet.GumpSelection;
import com.github.mayconr.juoserver.network.packet.SingleClickRequest;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.LongSupplier;

public final class UIModuleImpl implements UIModule {

    private static final String NEXT_CONTEXT_MENU_INDEX = "ui.nextContextMenuIndex";
    private static final String PENDING_CONTEXT_MENU = "ui.pendingContextMenu";
    private static final long CONTEXT_MENU_TIMEOUT_NANOS = Duration.ofSeconds(60).toNanos();
    private final LongSupplier clock;
    private final GumpSystem gumpSystem;
    private final WorldStorage storage;
    private final EventBus eventBus;
    private volatile List<ContextMenuEntry> contextMenuEntries;
    private ModuleContext.FlowFacade flows;

    public UIModuleImpl(
            GumpSystem gumpSystem,
            WorldStorage storage,
            EventBus eventBus,
            List<ContextMenuEntry> contextMenuEntries) {
        this(gumpSystem, storage, eventBus, contextMenuEntries, System::nanoTime);
    }

    UIModuleImpl(
            GumpSystem gumpSystem,
            WorldStorage storage,
            EventBus eventBus,
            List<ContextMenuEntry> contextMenuEntries,
            LongSupplier clock) {
        this.clock = Objects.requireNonNull(clock, "Clock is required");
        this.gumpSystem = gumpSystem;
        this.storage = storage;
        this.eventBus = eventBus;
        registerContextMenuEntries(contextMenuEntries);
    }

    @Override
    public void registerContextMenuEntries(List<ContextMenuEntry> entries) {
        contextMenuEntries = List.copyOf(entries);
    }

    @Override
    public void requestContextMenu(UOPlayer player, int serialId) {
        if (player == null) {
            return;
        }
        synchronized (player) {
            player.runtimeAttributes().remove(PENDING_CONTEXT_MENU);
        }
        resolveContextMenu(player, serialId).ifPresent(this::publishContextMenu);
    }

    private Optional<ContextMenuContext> resolveContextMenu(UOPlayer player, int serialId) {
        if (UOItem.isItem(serialId)) {
            return storage.items().get(serialId).map(item -> new ItemContextMenuContext(player, item));
        }
        if (UOMobile.isMobile(serialId)) {
            return storage.mobiles().get(serialId).map(mobile -> new MobileContextMenuContext(player, mobile));
        }
        return Optional.empty();
    }

    @Override
    public void onContextMenuSelection(UOPlayer player, int serialId, int index) {
        if (player == null) {
            return;
        }
        final PendingContextMenu pending;
        synchronized (player) {
            pending = (PendingContextMenu) player.runtimeAttributes().remove(PENDING_CONTEXT_MENU);
        }
        if (pending == null || pending.target().getSerialId() != serialId
                || clock.getAsLong() - pending.createdAt() >= CONTEXT_MENU_TIMEOUT_NANOS) {
            return;
        }
        var entry = pending.entries().get(index);
        if (entry != null) {
            executeContextMenuEntry(player, serialId, pending, entry);
        }
    }

    private void executeContextMenuEntry(
            UOPlayer player, int serialId, PendingContextMenu pending, ContextMenuEntry entry) {
        resolveContextMenu(player, serialId)
                .filter(context -> context.target() == pending.target())
                .filter(entry.predicate())
                .ifPresent(entry.callback());
    }

    private void publishContextMenu(ContextMenuContext context) {
        var entries = contextMenuEntries.stream()
                .filter(entry -> entry.predicate().test(context))
                .toList();
        if (entries.size() > 255) {
            throw new IllegalArgumentException("Context menu supports at most 255 visible entries");
        }
        var rendered = prepareContextMenu(context, entries);
        eventBus.publish(new ContextMenuRequested(context.player(), context.target().getSerialId(), rendered));
    }

    private List<ContextMenuRequested.Entry> prepareContextMenu(
            ContextMenuContext context, List<ContextMenuEntry> entries) {
        var rendered = new ArrayList<ContextMenuRequested.Entry>();
        var pending = new LinkedHashMap<Integer, ContextMenuEntry>();
        synchronized (context.player()) {
            for (var entry : entries) {
                int index = nextContextMenuIndex(context.player());
                pending.put(index, entry);
                rendered.add(new ContextMenuRequested.Entry(index, entry.cliloc()));
            }
            if (!pending.isEmpty()) {
                context.player().runtimeAttributes().set(PENDING_CONTEXT_MENU,
                        new PendingContextMenu(context.target(), Map.copyOf(pending), clock.getAsLong()));
            }
        }
        return List.copyOf(rendered);
    }

    /** Called under the player lock; zero is reserved and identifiers wrap after 65535. */
    private int nextContextMenuIndex(UOPlayer player) {
        var attributes = player.runtimeAttributes();
        int index = attributes.getOrDefault(NEXT_CONTEXT_MENU_INDEX, 1);
        attributes.set(NEXT_CONTEXT_MENU_INDEX, index == 0xFFFF ? 1 : index + 1);
        return index;
    }

    private record PendingContextMenu(
            UOObject<?> target, Map<Integer, ContextMenuEntry> entries, long createdAt) { }

    @Override
    public void update(double delta) {
        gumpSystem.update(delta);
    }

    @Override
    public void initialize(ModuleContext context) {
        this.flows = context.flows();
    }

    @Override
    public void tooltipRequest(UOPlayer player, List<Integer> serialIds) {
        var result = flows.execute(new TooltipContext(player, serialIds));
        if (result.flowFailed()) {
            throw ModuleOperationException.fromFailure("tooltipRequest", result, null);
        }
    }

    @Override
    public void openPaperdoll(UOPlayer player, int mobileSerial) {
        flows.execute(new OpenPaperdollContext(player, mobileSerial));
    }

    @Override
    public void doubleClick(UOPlayer player, DoubleClick doubleClick) {
        flows.execute(new DoubleClickContext(player, doubleClick));
    }

    @Override
    public void singleClick(UOPlayer player, SingleClickRequest request) {
        flows.execute(new SingleClickContext(player, request));
    }

    @Override
    public void sendSkillGump(UOPlayer player, int requestedSkillSerialId, SendSkillType type) {
        var result = flows.execute(new SkillGumpContext(player, requestedSkillSerialId,
                Objects.requireNonNull(type, "Skill packet type is required")));
        if (result.flowFailed()) {
            throw ModuleOperationException.fromFailure("sendSkillGump", result, null);
        }
    }

    @Override
    public void sendGump(UOPlayer player, DeclarativeGumpUI gumpUI, GumpHandler handler) {
        gumpSystem.send(player, gumpUI, handler);
    }

    @Override
    public void sendGump(UOPlayer player, DeclarativeGumpUI gumpUI, GumpHandler handler, double expirationSeconds) {
        gumpSystem.send(player, gumpUI, handler, expirationSeconds);
    }

    @Override
    public void onGumpSelection(UOPlayer player, GumpSelection gumpSelection) {
        gumpSystem.onGumpSelection(player, gumpSelection);
    }

    @Override
    public void sendStatusGump(UOPlayer player, int requestedStatusSerial) {
        var result = flows.execute(new StatusGumpContext(player, requestedStatusSerial));
        if (result.flowFailed()) {
            throw ModuleOperationException.fromFailure("sendStatusGump", result, null);
        }
    }
}
