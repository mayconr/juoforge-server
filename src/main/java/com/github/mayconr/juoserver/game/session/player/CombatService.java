package com.github.mayconr.juoserver.game.session.player;

import com.github.mayconr.juoserver.game.combat.CombatSystem;
import com.github.mayconr.juoserver.game.model.CharacterStatus;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.model.WarModeType;
import com.github.mayconr.juoserver.network.packet.AttackCharacter;
import com.github.mayconr.juoserver.network.packet.RequestWarMode;
import com.github.mayconr.juoserver.network.packet.UpdateMobileStatus;
import com.github.mayconr.juoserver.network.packet.UpdatePlayer;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.group.ChannelGroup;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CombatService {
    private final UOPlayer player;
    private final ChannelGroup channelGroup;
    private final ChannelHandlerContext ctx;
    private final CombatSystem combatSystem;

    void handleWarMode(WarModeType type) {
        if (WarModeType.NORMAL.equals(type)) {
            player.setStatus(CharacterStatus.NORMAL);
            if (combatSystem.isAttacking(player.getSerialId())) {
                combatSystem.cancelAttack(player.getSerialId());
                ctx.write(new AttackCharacter(0));
            }
        } else if (WarModeType.FIGHTING.equals(type)) {
            player.setStatus(CharacterStatus.WAR_MODE);
        }
        ctx.writeAndFlush(new RequestWarMode(type));
        channelGroup.writeAndFlush(new UpdatePlayer(player));
    }

    void handleAttack(int opponentSerialId) {
        combatSystem.requestAttack(player.getSerialId(), opponentSerialId);
        // TODO validate (range, LOS, cooldown, warmode, stamina, flags)
        channelGroup.writeAndFlush(
                new UpdateMobileStatus(opponentSerialId, player.getSerialId()),
                channel -> !channel.equals(ctx.channel()));
    }
}
