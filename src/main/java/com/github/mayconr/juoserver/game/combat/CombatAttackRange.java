package com.github.mayconr.juoserver.game.combat;

import com.github.mayconr.juoserver.game.mobile.WorldMobile;
import com.github.mayconr.juoserver.game.model.Layer;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.UOMobile;

public final class CombatAttackRange {
    private CombatAttackRange() {}

    public static int resolve(UOMobile attacker, WorldMobile mobiles) {
        var serials = attacker.getEquippedItems();
        if (serials == null || (!serials.containsKey(Layer.ONE_HANDED) && !serials.containsKey(Layer.TWO_HANDED))) return 1;
        var items = mobiles.getEquippedItems(attacker);
        var weapon = items.get(Layer.ONE_HANDED);
        return forWeapon(weapon == null ? items.get(Layer.TWO_HANDED) : weapon);
    }

    public static int forWeapon(UOItem weapon) {
        return weapon != null && weapon.getTemplate().weapon() != null ? weapon.getTemplate().weapon().radius() : 1;
    }
}
