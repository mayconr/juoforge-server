package com.github.mayconr.juoserver.game.combat;

import com.github.mayconr.juoserver.game.model.Layer;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.model.WeaponStyle;
import com.github.mayconr.juoserver.infrastructure.storage.WorldStorage;

public final class EquippedWeaponResolver {
    private EquippedWeaponResolver() {}

    public static EquippedWeapon resolve(UOMobile mobile, WorldStorage storage) {
        var equipment = mobile.getEquippedItems();
        Integer serial = equipment == null ? null : equipment.get(Layer.ONE_HANDED);
        if (serial == null && equipment != null) {
            serial = equipment.get(Layer.TWO_HANDED);
        }
        if (serial == null) {
            return new EquippedWeapon(null, WeaponStyle.WRESTLING);
        }

        var item = storage.items().get(serial)
                .orElseThrow(() -> new IllegalArgumentException("Weapon not found"));
        var template = item.getTemplate();
        var weapon = template == null ? null : template.weapon();
        return new EquippedWeapon(item, weapon == null ? null : weapon.style());
    }

    public record EquippedWeapon(UOItem item, WeaponStyle style) {}
}
