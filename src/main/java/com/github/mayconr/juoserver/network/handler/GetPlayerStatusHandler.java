package com.github.mayconr.juoserver.network.handler;

import com.github.mayconr.juoserver.game.model.Race;
import com.github.mayconr.juoserver.game.session.player.PlayerSession;
import com.github.mayconr.juoserver.network.packet.GetPlayerStatus;
import com.github.mayconr.juoserver.network.packet.StatusBarInfo;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;

@ChannelHandler.Sharable
public class GetPlayerStatusHandler extends PlayerSessionChannelInboundHandler<GetPlayerStatus> {
    @Override
    protected void channelRead0(
            PlayerSession session, ChannelHandlerContext ctx, GetPlayerStatus msg) {
        System.out.println("enviando status");
        final var player = session.getPlayer();
        player.setHitpoints(7);
        player.setMaxHitpoints(100);
        player.setStrength(40);
        player.setDexterity(50);
        player.setIntelligence(70);

        player.setStamina(10);
        player.setMaxStamina(90);

        player.setMana(11);
        player.setMaxMana(80);

        player.setGold(1000);
        player.setWeight(11);

        // ml
        player.setMaxWeight(120);
        player.setRace(Race.HUMAN);

        // ulr
        player.setStatCap(300);
        player.setFollowers(1);
        player.setMaxFollowers(3);

        // kr
        player.setPhysicalResist(1);
        player.setPoisonResist(2);
        player.setColdResist(3);
        player.setEnergyResist(5);
        player.setFireResist(6);
        player.setLuck(99);

        player.setDamageMin(1);
        player.setDamageMax(32);

        player.setLowerManaCost(11);
        player.setFasterCasting(22);
        player.setFasterCastRecovery(33);
        player.setMaxFireResist(10);
        player.setMaxColdResist(19);
        player.setMaxPhysicalResist(22);
        player.setMaxPoisonResist(44);
        player.setMaxEnergyResist(33);

        player.setDefenseChanceIncrease(41);
        player.setMaxDefenseChanceIncrease(43);

        player.setSpellDamageIncrease(111);
        player.setSwingSpeedIncrease(123);
        player.setLowerReagentCost(333);
        player.setWeaponDamageIncrease(33);

        player.setHitChanceIncrease(443);

        ctx.writeAndFlush(new StatusBarInfo(session.getPlayer()));
    }
}
