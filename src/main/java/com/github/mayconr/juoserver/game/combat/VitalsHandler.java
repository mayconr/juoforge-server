package com.github.mayconr.juoserver.game.combat;

import com.github.mayconr.juoserver.JuoforgeConfiguration;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.model.event.VitalsChanged;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class VitalsHandler {

    public static final String MOBILE_HP_ACCUMULATOR = "mobile.hpAccumulator";
    public static final String MOBILE_STAMINA_ACCUMULATOR = "mobile.staminaAccumulator";
    public static final String MOBILE_MANA_ACCUMULATOR = "mobile.manaAccumulator";
    private final EventBus eventBus;
    private final JuoforgeConfiguration configuration;

    public void regen(UOMobile mobile, double interval) {
        // Mutable variables
        boolean dirty = false;
        double hpAcc = mobile.getPersistentAttribute(MOBILE_HP_ACCUMULATOR, 0.0);
        double staminaAcc = mobile.getPersistentAttribute(MOBILE_STAMINA_ACCUMULATOR, 0.0);
        double manaAcc = mobile.getPersistentAttribute(MOBILE_MANA_ACCUMULATOR, 0.0);

        final double min = 0.1;
        final int saturationFactor = configuration.settings().vitals().saturationFactor();

        final double hpRegenBase = (min + ((double) mobile.getStrength() / (mobile.getStrength() + saturationFactor)));
        hpAcc += hpRegenBase * interval;

        final int hpGain = (int) hpAcc;
        final int hp = mobile.getHitpoints() + hpGain;
        if (hpGain > 0 && hp <= mobile.getMaxHitpoints()) {
            if (log.isDebugEnabled()) {
                log.info("Player [{}-{}] has Regen {} hp", mobile.getSerialId(), mobile.getName(), hpGain);
            }
            hpAcc -= hpGain;
            mobile.setHitpoints( hp );
            dirty = true;
        }

        final double manaRegenBase = (min + ((double) mobile.getIntelligence() / (mobile.getIntelligence() + saturationFactor)));
        manaAcc += manaRegenBase * interval;
        final int manaGain = (int) manaAcc;
        int mana = mobile.getMana() + manaGain;
        if (manaGain > 0 && mana <= mobile.getMaxMana()) {
            if (log.isDebugEnabled()) {
                log.info("Player [{}-{}] has Regen {} mana", mobile.getSerialId(), mobile.getName(), manaGain);
            }
            manaAcc -= manaGain;
            mobile.setMana( mana );
            dirty = true;
        }

        final double staminaRegenBase = (min + ((double) mobile.getDexterity() / (mobile.getDexterity() + saturationFactor)));
        staminaAcc += staminaRegenBase * interval;
        final int staminaGain = (int) staminaAcc;
        final int stamina = mobile.getStamina() + staminaGain;
        if (staminaGain > 0 && stamina <= mobile.getMaxStamina()) {
            if (log.isDebugEnabled()) {
                log.info("Player [{}-{}] has Regen {} stamina", mobile.getSerialId(), mobile.getName(), staminaGain);
            }
            staminaAcc -= staminaGain;
            mobile.setStamina( stamina );
            dirty = true;
        }

        mobile.setPersistentAttribute(MOBILE_HP_ACCUMULATOR, hpAcc);
        mobile.setPersistentAttribute(MOBILE_STAMINA_ACCUMULATOR, staminaAcc);
        mobile.setPersistentAttribute(MOBILE_MANA_ACCUMULATOR, manaAcc);

        if (dirty) {
            eventBus.publish(new VitalsChanged(mobile));
        }
    }

}
