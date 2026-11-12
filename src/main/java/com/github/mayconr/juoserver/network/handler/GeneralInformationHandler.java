package com.github.mayconr.juoserver.network.handler;

import com.github.mayconr.juoserver.game.world.WorldInternal;
import com.github.mayconr.juoserver.infrastructure.i18n.ClientLocale;
import com.github.mayconr.juoserver.network.packet.ClientTypeExtendedCommand;
import com.github.mayconr.juoserver.network.packet.ClientVersionExtendedCommand;
import com.github.mayconr.juoserver.network.packet.CloseStatusExtendedCommand;
import com.github.mayconr.juoserver.network.packet.ContextMenuRequestExtendedCommand;
import com.github.mayconr.juoserver.network.packet.ContextMenuSelectionExtendedCommand;
import com.github.mayconr.juoserver.network.packet.GeneralInformation;
import com.github.mayconr.juoserver.network.packet.LanguageExtendedCommand;
import com.github.mayconr.juoserver.network.packet.ScreenSizeExtendedCommand;
import com.github.mayconr.juoserver.network.packet.SpellSelectionExtendedCommand;
import com.github.mayconr.juoserver.network.packet.UnknownExtendedCommand;
import com.github.mayconr.juoserver.network.session.PlayerSession;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import java.util.HexFormat;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ChannelHandler.Sharable
@RequiredArgsConstructor
public class GeneralInformationHandler
        extends PlayerSessionChannelInboundHandler<GeneralInformation> {
    private final WorldInternal world;

    @Override
    protected void channelRead0(
            PlayerSession session, ChannelHandlerContext ctx, GeneralInformation msg) {
        var command = msg.getCommand();
        if (command instanceof LanguageExtendedCommand language) {
            var locale = ClientLocale.fromCode(language.language());
            session.setLocale(locale);
            return;
        }
        var details = switch (command) {
            case ContextMenuSelectionExtendedCommand selection -> "Context menu target: 0x%08X | Entry: %d"
                    .formatted(selection.serialId(), selection.index());
            case ContextMenuRequestExtendedCommand menu -> "Context menu target: 0x%08X".formatted(menu.serialId());
            case ClientTypeExtendedCommand type -> "Marker: 0x%02X | Expansion flags: 0x%08X".formatted(
                    type.marker(), type.flags());
            case ClientVersionExtendedCommand version -> "Version: " + version.version();
            case CloseStatusExtendedCommand status -> "Character serial: %d (0x%08X)".formatted(
                    status.serialId(), status.serialId());
            case LanguageExtendedCommand language -> "Language: " + language.language().replace("\0", "");
            case ScreenSizeExtendedCommand screen -> "Width: %d | Height: %d".formatted(
                    screen.width(), screen.height());
            case SpellSelectionExtendedCommand spell -> "Action: %d | Spell ID: %d".formatted(
                    spell.action(), spell.spellId());
            case UnknownExtendedCommand unknown -> "Payload (hex): " + HexFormat.ofDelimiter(" ").formatHex(unknown.payload());
        };
        log.info("General Information | Subcommand: 0x{} | Type: {} | Length: {} bytes | {}",
                HexFormat.of().toHexDigits((short) msg.getSubCommand()),
                command.getClass().getSimpleName(), msg.getLength(), details);
        if (command instanceof ContextMenuRequestExtendedCommand menu) {
            world.ui().requestContextMenu(session.getPlayer(), menu.serialId());
            return;
        }
        if (command instanceof ContextMenuSelectionExtendedCommand selection) {
            world.ui().onContextMenuSelection(session.getPlayer(), selection.serialId(), selection.index());
            return;
        }
        if (command instanceof ClientTypeExtendedCommand type) {
            session.setClientExpansionFlags(type.flags());
            return;
        }
        if (command instanceof SpellSelectionExtendedCommand spell) {
            var caster = session.getPlayer();
            if (caster == null) {
                log.warn("Ignoring spell cast request without an active player | Spell ID: {}", spell.spellId());
                return;
            }
            world.spell().getSpellByClientId(spell.spellId()).ifPresentOrElse(
                    template -> world.spell().castSpell(caster, template.key()),
                    () -> log.warn("Unknown client spell ID: {} | Caster serial: {}", spell.spellId(), caster.getSerialId()));
        }
    }

}
