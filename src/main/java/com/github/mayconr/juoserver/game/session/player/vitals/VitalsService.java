package com.github.mayconr.juoserver.game.session.player.vitals;

import com.github.mayconr.juoserver.ServerProperties;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.session.SessionOutbound;
import com.github.mayconr.juoserver.network.packet.StatusBarInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class VitalsService {

    private final UOPlayer player;
    private final SessionOutbound outbound;
    private final ServerProperties properties;
    private double hpAcc;
    private double staminaAcc;
    private double manaAcc;

    public void regen(double interval) {
        boolean dirty = false;
        final double min = 0.1;
        final int saturationFactor = properties.vitals().saturationFactor();

        final double hpRegenBase = (min + ((double) player.getStrength() / (player.getStrength() + saturationFactor)));
        hpAcc += hpRegenBase * interval;

        final int hpGain = (int) hpAcc;
        final int hp = player.getHitpoints() + hpGain;
        if (hpGain > 0 && hp <= player.getMaxHitpoints()) {
            if (log.isDebugEnabled()) {
                log.info("Player [{}-{}] has Regen {} hp", player.getSerialId(), player.getName(), hpGain);
            }
            hpAcc -= hpGain;
            player.setHitpoints( hp );
            dirty = true;
        }

        final double manaRegenBase = (min + ((double) player.getIntelligence() / (player.getIntelligence() + saturationFactor)));
        manaAcc += manaRegenBase * interval;
        final int manaGain = (int) manaAcc;
        int mana = player.getMana() + manaGain;
        if (manaGain > 0 && mana <= player.getMaxMana()) {
            if (log.isDebugEnabled()) {
                log.info("Player [{}-{}] has Regen {} mana", player.getSerialId(), player.getName(), manaGain);
            }
            manaAcc -= manaGain;
            player.setMana( mana );
            dirty = true;
        }

        final double staminaRegenBase = (min + ((double) player.getDexterity() / (player.getDexterity() + saturationFactor)));
        staminaAcc += staminaRegenBase * interval;
        final int staminaGain = (int) staminaAcc;
        final int stamina = player.getStamina() + staminaGain;
        if (staminaGain > 0 && stamina <= player.getMaxStamina()) {
            if (log.isDebugEnabled()) {
                log.info("Player [{}-{}] has Regen {} stamina", player.getSerialId(), player.getName(), staminaGain);
            }
            staminaAcc -= staminaGain;
            player.setStamina( stamina );
            dirty = true;
        }

        if (dirty) {
            outbound.writeAndFlush(new StatusBarInfo(player));
        }
    }

}
