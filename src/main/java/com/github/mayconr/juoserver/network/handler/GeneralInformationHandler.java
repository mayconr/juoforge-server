package com.github.mayconr.juoserver.network.handler;

import com.github.mayconr.juoserver.network.packet.GeneralInformation;
import com.github.mayconr.juoserver.network.packet.ClientVersionExtendedCommand;
import com.github.mayconr.juoserver.network.packet.CloseStatusExtendedCommand;
import com.github.mayconr.juoserver.network.packet.LanguageExtendedCommand;
import com.github.mayconr.juoserver.network.packet.ScreenSizeExtendedCommand;
import com.github.mayconr.juoserver.network.packet.SpellSelectionExtendedCommand;
import com.github.mayconr.juoserver.network.packet.UnknownExtendedCommand;
import com.github.mayconr.juoserver.network.session.PlayerSession;
import com.github.mayconr.juoserver.game.world.WorldInternal;
import lombok.RequiredArgsConstructor;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import java.util.HexFormat;

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
        var details = switch (command) {
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
        if (command instanceof SpellSelectionExtendedCommand spell) {
            var caster = session.getPlayer();
            if (caster == null) {
                log.warn("Ignoring spell cast request without an active player | Spell ID: {}", spell.spellId());
                return;
            }
            world.getSpellByClientId(spell.spellId()).ifPresentOrElse(
                    template -> world.castSpell(caster, template.key()),
                    () -> log.warn("Unknown client spell ID: {} | Caster serial: {}", spell.spellId(), caster.getSerialId()));
        }
    }
}
