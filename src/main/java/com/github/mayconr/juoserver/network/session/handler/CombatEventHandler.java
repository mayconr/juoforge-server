package com.github.mayconr.juoserver.network.session.handler;

import com.github.mayconr.juoserver.network.session.SessionView;
import com.github.mayconr.juoserver.network.session.SessionPacketWriter;

import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.game.model.event.*;
import com.github.mayconr.juoserver.network.packet.*;
import com.github.mayconr.juoserver.game.world.WorldInternal;
import lombok.extern.slf4j.Slf4j;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public final class CombatEventHandler {
    private final SessionView session;
    private final SessionPacketWriter packetWriter;
    private final WorldInternal world;

    public CombatEventHandler(SessionView session, SessionPacketWriter packetWriter, WorldInternal world) {
        this.session = session;
        this.packetWriter = packetWriter;
        this.world = world;
    }

    public void onAnimationSent(AnimationSent event) {
        if (!session.shouldReceiveUpdate(event.mobile())) {
            return;
        }
        packetWriter.enqueue(packets -> {
            packets.writeAndFlush(new CharacterAnimation(event.mobile(), event.options().repeat(), event.options().type(), event.options().frame(), event.options().direction()));
        });
    }

    public void onMobileStatusChanged(MobileStatusChanged event) {
        if (session.isPlayer(event.mobile())) {
            // abort any kind of combat
            if (CharacterStatus.WAR_MODE.equals(event.oldStatus())) {
                packetWriter.write(new AttackCharacter(0));
            }

            packetWriter.writeAndFlush(new RequestWarMode(event.newStatus().getWarModeType()));
        }
        if (event.mobile() instanceof UOPlayer player && session.shouldReceiveUpdate(player)) {
            packetWriter.writeAndFlush(new UpdatePlayer(player));
        }
    }

    public void onMobileDamaged(MobileDamagedEvent event) {
        if (session.isPlayer(event.source()) || session.isPlayer(event.target())) {
            packetWriter.enqueue(packets -> {
                packets.write(new Damage(event.target(), event.totalDamage()));
                packets.writeAndFlush(new StatusBarInfo(event.target()));
            });
        }
    }

    public void onMobileDeath(MobileDeathEvent event) {
        if (!session.isPlayer(event.target()) && !session.shouldReceiveUpdate(event.target())) {
            return;
        }
        packetWriter.enqueue(packets -> {
            final var corpse = (UOCorpse) event.corpse();
            final var target = event.target();

            // Remove transferred items from the client's old equipment/container view first.
            for (var serial : corpse.getContainerItems()) packets.write(new DeleteObject(serial));
            packets.write(new UpdateHealth(target));
            packets.write(new ObjectInfo(corpse));
            // Send corpse items
            final List<CorpseClothing.Entry> items = new ArrayList<>();
            var equippedItems = corpse.getEquippedItems();
            for (var entry : equippedItems.entrySet()) {
                var item = world.item().getItemBySerialId(entry.getValue())
                        .orElse(null);

                if (item != null) {
                    items.add(new CorpseClothing.Entry(entry.getKey(), item));
                    packets.write(new ObjectInfo(item));
                }
            }
            packets.write(new CorpseClothing(corpse, items));
            packets.write(new DeathAction(target, corpse.getSerialId()));

            if (session.isPlayer(target)) {
                packets.write(new DeathScreen(DeathScreenType.SERVER));
                packets.write(new DrawGamePlayer(target));
            }
            if (target instanceof UOPlayer && MobileVisibility.isPresent(target)) {
                packets.write(new DrawMobile(target, world.mobile().getEquippedItems(target)));
            }
            packets.flush();
        });
    }

    public void onMobileResurrect(MobileResurrectEvent event) {
        if (session.player() == null) {
            return;
        }
        var target = event.mobile();
        if (!MobileVisibility.isPresent(target)) {
            return;
        }
        if (!session.isPlayer(target) && !session.shouldReceiveUpdate(target)) {
            return;
        }
        packetWriter.enqueue(packets -> {
            if (session.isPlayer(target)) {
                packets.write(new StatusBarInfo(target));
                packets.write(new DeathScreen(DeathScreenType.RESURRECT));
                packets.write(new DrawGamePlayer(target));
            }
            packets.writeAndFlush(new DrawMobile(target, world.mobile().getEquippedItems(target)));
        });
    }

    public void onCombatStarted(CombatStarted event) {
        packetWriter.enqueue(packets -> {
            //packets.write(new CombatantChange(combatStarted.target(), combatStarted.attacker()));
            // arrows: 0x0F3E 0x0F3F
            // bolt 0x1BFB

        });
    }

    public void onCombatOccurring(CombatOccurring event) {
        if (!session.shouldReceiveUpdate(event.attacker()) && !session.shouldReceiveUpdate(event.target())) {
            return;
        }
        packetWriter.enqueue(packets -> {
            var attacker = event.attacker();
            var target = event.target();
            int hitFrame = event.hitFrame();
            int animationFrame = hitFrame * 2;

            switch (event.type()) {
                case CombatOccurring.MeleeType meleeType -> {
                    packets.write(CharacterAnimationFactory.weapon(attacker, meleeType.weaponItem().getTemplate().weapon(), animationFrame));
                }
                case CombatOccurring.WrestlingType wrestlingType -> {
                    packets.write(CharacterAnimationFactory.wrestling(attacker, animationFrame));
                }
                case CombatOccurring.RangedType rangedType -> {
                    packets.write(CharacterAnimationFactory.weapon(attacker, rangedType.weaponItem().getTemplate().weapon(), animationFrame));
                    packets.write(new GraphicalEffectPacket(
                            EffectType.MOVING,
                            0x1BFE,
                            attacker,
                            target,
                            0,
                            0,
                            false,
                            false
                    ));
                }
                case CombatOccurring.SpellType spellType -> throw new IllegalStateException("Spell not supported ");
            }
            packets.flush();
        });
    }
}
