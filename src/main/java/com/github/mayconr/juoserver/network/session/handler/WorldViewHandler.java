package com.github.mayconr.juoserver.network.session.handler;

import com.github.mayconr.juoserver.network.session.SessionView;
import com.github.mayconr.juoserver.network.session.SessionPacketWriter;

import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.game.model.event.*;
import com.github.mayconr.juoserver.network.packet.*;
import com.github.mayconr.juoserver.game.world.WorldInternal;
import lombok.extern.slf4j.Slf4j;
import java.util.List;
import com.github.mayconr.juoserver.game.GamePlaySettings;

@Slf4j
public final class WorldViewHandler {
    private final SessionView session;
    private final SessionPacketWriter packetWriter;
    private final WorldInternal world;
    private final GamePlaySettings settings;

    public WorldViewHandler(SessionView session, SessionPacketWriter packetWriter, WorldInternal world, GamePlaySettings settings) {
        this.session = session;
        this.packetWriter = packetWriter;
        this.world = world;
        this.settings = settings;
    }

    public void onMobileMoved(MobileMoved moved) {
        if (session.player() == null) {
            return;
        }
        final UOMobile mobile = moved.mobile();

        // =========================
        // Case 1: Other mobile moved
        // =========================
        if (!session.isPlayer(mobile)) {
            handleOtherMobileMovement(mobile);
            return;
        }

        // =========================
        // Case 2: Player moved
        // =========================
        handlePlayerMovement(moved);
    }

    private void handleOtherMobileMovement(UOMobile mobile) {
        if (!session.shouldReceiveUpdate(mobile)) {
            return;
        }
        if (!MobileVisibility.isVisible(mobile)) {
            return;
        }
        packetWriter.enqueue(packets -> {
            if (session.shouldReceiveUpdate(mobile) && MobileVisibility.isVisible(mobile)) {
                packets.writeAndFlush(new DrawMobile(mobile, world.mobile().getEquippedItems(mobile)));
            }
        });
    }

    private void handlePlayerMovement(MobileMoved moved) {
        var player = session.player();
        var nearbyView = loadNearbyView(player);

        // Acknowledge movement
        packetWriter.write(new MoveResyncAck(moved.sequence(), session.player().getNotoriety()));

        writeNearbyView(player, nearbyView);

        // Update the player itself
        if (moved.teleport()) {
            packetWriter.write(new DrawGamePlayer(session.player()));
        } else {
            packetWriter.write(new UpdatePlayer(session.player()));
        }

        packetWriter.flush();

        // TODO:
        // - Send incremental updates instead of full redraw
        // - Track enter/leave range to avoid resending everything
    }

    private NearbyView loadNearbyView(UOPlayer player) {
        int range = settings.world().visibility().range();
        return new NearbyView(
                world.storage().getMobilesInRange(player, range, MobileVisibility::isVisible),
                world.item().getItemsInRange(player, range));
    }

    /** Writes nearby entities without flushing; the caller owns the surrounding packet sequence. */
    private void writeNearbyView(UOPlayer player, NearbyView nearbyView) {
        for (UOMobile mobile : nearbyView.mobiles()) {
            if (!mobile.equals(player) && MobileVisibility.isVisible(mobile)) {
                packetWriter.write(new DrawMobile(mobile, world.mobile().getEquippedItems(mobile)));
            }
        }
        for (UOItem item : nearbyView.items()) {
            packetWriter.write(new ObjectInfo(item));
        }
    }

    private record NearbyView(List<UOMobile> mobiles, List<UOItem> items) { }

    public void onMobileMoveRejected(MobileMoveRejected rejected) {
        if (session.isPlayer(rejected.mobile())) {
            packetWriter.enqueue(packets -> {
                packets.write(new MoveReject(rejected.sequence(), rejected.mobile()));
                packets.writeAndFlush(new DrawGamePlayer(session.player()));
            });
        }
    }

    public void onMobileResynced(MobileMoveResync resync) {
        if (session.isPlayer(resync.player())) {
            packetWriter.enqueue(packets -> {
                log.info("Mobile resynced");
                packets.write(new MoveResyncAck(resync.sequence(), resync.player().getNotoriety()));
                packets.writeAndFlush(new DrawGamePlayer(session.player()));
            });
        }
    }

    public void onEnteredLineOfSight(MobileEnteredLineOfSight event) {
        if (session.player() != null && session.isPlayer(event.target())) {
            var mobile = event.observer();
            if (!MobileVisibility.isVisible(mobile)) {
                return;
            }

            packetWriter.writeAndFlush(new DrawMobile(mobile, world.mobile().getEquippedItems(mobile)));
        }
    }

    public void onNpcCreated(NpcCreated event) {
        if (!session.shouldReceiveUpdate(event.npc())) {
            return;
        }
        packetWriter.writeAndFlush(new DrawMobile(event.npc(), world.mobile().getEquippedItems(event.npc())));
    }

    public void onMobileDeleted(NpcRemoved event) {
        packetWriter.writeAndFlush(new DeleteObject(event.npc()));
    }

    public void onPlayerDeleted(PlayerDeleted event) {
        packetWriter.enqueue(packets -> packets.writeAndFlush(new DeleteObject(event.deletedPlayer())));
    }

    public void onPlayerLoggedIn(PlayerLoggedIn event) {
        if (session.player() != null && session.isPlayer(event.player())) {
            var player = session.player();
            var nearbyView = loadNearbyView(player);

            packetWriter.write(new LoginConfirm(session.player(), 7168, 4096));
            packetWriter.write(new SeasonalInformation(Season.Summer, true));

            writeNearbyView(player, nearbyView);

            packetWriter.write(new SendSkill(session.player()));
            packetWriter.write(new DrawGamePlayer(session.player()));
            packetWriter.write(new DrawMobile(session.player(), world.mobile().getEquippedItems(session.player())));
            packetWriter.write(new StatusBarInfo(session.player()));
            packetWriter.write(new LoginComplete());
            packetWriter.flush();

        }
    }

    public void onPlayerLoggedOut(PlayerLoggedOut event) {
        if (session.isPlayer(event.player())) {
            // do not send anything. User has logged off
            return;
        }
        packetWriter.writeAndFlush(new DeleteObject(event.player()));
    }
}
