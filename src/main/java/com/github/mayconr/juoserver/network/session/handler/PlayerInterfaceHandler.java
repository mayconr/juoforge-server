package com.github.mayconr.juoserver.network.session.handler;

import com.github.mayconr.juoserver.network.session.SessionView;
import com.github.mayconr.juoserver.network.session.SessionPacketWriter;

import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.game.model.event.*;
import com.github.mayconr.juoserver.network.packet.*;
import com.github.mayconr.juoserver.game.world.WorldInternal;
import lombok.extern.slf4j.Slf4j;
import com.github.mayconr.juoserver.game.model.event.message.*;
import com.github.mayconr.juoserver.network.session.i18n.*;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public final class PlayerInterfaceHandler {
    private final SessionView session;
    private final SessionPacketWriter packetWriter;
    private final WorldInternal world;
    private final MessageLocalizer localizer = new ResourceBundleMessageLocalizer("messages");

    public PlayerInterfaceHandler(SessionView session, SessionPacketWriter packetWriter, WorldInternal world) {
        this.session = session;
        this.packetWriter = packetWriter;
        this.world = world;
    }

    public void onMobileSpeech(MobileSpeech event) {
        if (session.shouldReceiveUpdate(event.mobile())) {
            packetWriter.writeAndFlush(new SendSpeech(event));
        }
    }

    public void onObjectLabelRequested(ObjectLabelRequested event) {
        if (!session.isPlayer(event.player())) return;
        var object = event.object();
        var name = object.getDisplayName();
        if (name == null || name.isBlank()) name = object.getName();
        if (name == null || name.isBlank()) return;
        packetWriter.writeAndFlush(new SendSpeech(TextType.LABEL, 0x03B2, object.getSerialId(),
                object.getModelId(), 3, name, name));
    }

    public void onMessageSent(MessageSent event) {
        var speakerId = 0;
        var speakerName = "";
        var speakerModel = 0;
        var style = event.messageStyle();

        if (event.messageSource() != null) {
            speakerId = event.messageSource().getSerialId();
            speakerName = event.messageSource().getDisplayName();
            speakerModel = event.messageSource().getModelId();
        }

        final String text = switch (event.messageContent()) {
            case PlainTextMessageContent plain -> plain.text();
            case LocalizedMessageContent localized -> localizer.localize(localized, session.locale());
        };
        final var speech = new SendSpeech(style.type(), style.hue(), speakerId, speakerModel, style.font(), speakerName, text);

        if (style.type().equals(TextType.BROADCAST)) {
            packetWriter.writeAndFlush(speech);
        } else {
            if (event.messageTarget() != null) {
                if (session.isPlayer(event.messageTarget())) {
                    packetWriter.writeAndFlush(speech);
                }
            } else {
                if (event.messageSource() != null && session.shouldReceiveUpdate(event.messageSource())) {
                    packetWriter.writeAndFlush(speech);
                }
            }
        }
    }

    public void onTargetSent(TargetSent event) {
        if (session.isPlayer(event.player())) {
            packetWriter.writeAndFlush(new Target(event.id(), event.target(), event.type()));
        }
    }

    public void onPaperdollOpened(PaperdollOpened event) {
        if (session.isPlayer(event.player())) {
            packetWriter.writeAndFlush(new OpenPaperdoll(event.paperdoll(), event.flag()));
        }
    }

    public void onContainerOpened(ContainerOpenedEvent event) {
        if (session.isPlayer(event.player())) {
            final var container = event.container();

            packetWriter.write(new DrawContainer(container));
            if (!container.getContainerItems().isEmpty()) {
                var items = new ArrayList<UOItem>(container.getContainerItems().size());
                for (var item : container.getContainerItems()) {
                    world.item().getItemBySerialId(item).ifPresent(items::add);
                }
                packetWriter.write(AddMultipleItemsToContainer.ofUOItem(container, items));
            }
            packetWriter.flush();
        }
    }

    public void onStatusGumpRequested(StatusGumpRequested event) {
        if (session.isPlayer(event.player())) {
            packetWriter.writeAndFlush(new StatusBarInfo(event.requestedFor()));
        }
    }

    public void onTooltipRequested(TooltipRequested event) {
        if (session.isPlayer(event.player())) {
            for (TooltipSupport object : event.objects()) {
                packetWriter.writeAndFlush(new TooltipRequest(object));
            }
        }
    }

    public void onGumpSent(GumpSent event) {
        if (session.isPlayer(event.player())) {
            packetWriter.writeAndFlush(new SendGumpDialog(event.player(), event.gumpId(), 100, 100, event.builtGump().layout, event.builtGump().texts));
        }
    }

    public void onSpellBookOpened(SpellBookOpened event) {
        if (session.player() == null || !session.isPlayer(event.player())) {
            return;
        }
        packetWriter.enqueue(packets -> {
            if (packetWriter.isActive()) {
                packets.write(new GeneralInformation(new NewSpellbookExtendedCommand(
                        event.bookSerialId(), event.modelId(), event.type(), event.spellMask())));
                packets.writeAndFlush(new DrawContainer(event.bookSerialId(), 0xFFFF));
            }
        });
    }

    public void onRawPacketSent(RawPacketSent event) {
        sendDebugPackets(event.player(), List.of(event.packet()));
    }

    public void onRawPacketsSent(RawPacketsSent event) {
        sendDebugPackets(event.player(), event.packets());
    }

    public void onSkillUpdated(SkillUpdated event) {
        if (session.isPlayer(event.player())) {
            packetWriter.writeAndFlush(new SendSkill(SendSkillType.SINGLE_UPDATE_WITH_CAP, List.of(event.skill())));
        }
    }

    public void onSkillGained(SkillGained event) {
        if (session.isPlayer(event.mobile())) {
            packetWriter.writeAndFlush(new SendSkill(SendSkillType.SINGLE_UPDATE, List.of(event.skill())));
        }
    }

    public void onSkillGumpRequested(SkillGumpRequested event) {
        if (session.isPlayer(event.player())) {
            log.info("Sending skills of {}", event.skillsOf());
            packetWriter.writeAndFlush(new SendSkill(event.type(), event.skillsOf().getSkills().getSkillValues()));
        } else {
            log.info("Not implemented yet");
        }

        if (session.player() != null && log.isDebugEnabled()) {
            log.debug("Sending skill gump for [{}-{}]", session.player().getSerialId(), session.player().getName());
        }
    }

    public void onSkillLocked(SkillLocked event) {
        if (session.isPlayer(event.mobile())) {
            packetWriter.writeAndFlush(new SendSkill(SendSkillType.SINGLE_UPDATE, event.skills()));
        }
    }

    public void onVendorTradeSessionOpened(VendorSessionOpened event) {
        if (session.isPlayer(event.player())) {
            final var vendor = event.vendor();
            final var sellContainerSerialId = vendor.getEquippedItems().get(Layer.SHOP_BUY_RESTOCK);
            final var sellContainer = world.item().getContainerBySerialId(sellContainerSerialId)
                    .orElseThrow(() -> new RuntimeException("Sell container not found for serial "+sellContainerSerialId));

            packetWriter.write(AddMultipleItemsToContainer.ofStockItem(sellContainer, event.session().items().values()));
            packetWriter.write(new VendorBuyList(sellContainer, event.session().items().values()));
            packetWriter.write(new DrawContainer(vendor.getSerialId(), 0x0030));
            packetWriter.flush();
            // TODO locate items for uocontainer
        }
    }

    public void onVitalsChanged(VitalsChanged event) {
        if (session.isPlayer(event.mobile())) {
            packetWriter.writeAndFlush(new StatusBarInfo(session.player()));
        }
    }

    public void onMobileGoldChanged(MobileGoldChanged event) {
        if (session.isPlayer(event.mobile())) {
            packetWriter.writeAndFlush(new StatusBarInfo(session.player()));
        }
    }

    private void sendDebugPackets(UOPlayer recipient, List<Packet> batch) {
        if (session.player() == null || !session.player().getId().equals(recipient.getId()) || batch.isEmpty()) {
            return;
        }
        packetWriter.enqueue(packets -> {
            if (packetWriter.isActive()) {
                for (var packet : batch) {
                    packets.write(packet);
                    log.debug("Raw packet [{}] sent to client.", packet);
                }
                packets.flush();
            }
        });
    }
}
