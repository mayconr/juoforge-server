package com.github.mayconr.juoserver.game.combat;

import com.github.mayconr.juoserver.game.model.WeaponStyle;

/** Default weapon skill mapping shared by hit checks and skill gain rules. */
public final class CombatWeaponSkillResolver {
    private CombatWeaponSkillResolver() {}

    public static int forStyle(WeaponStyle style) {
        return switch (style) {
            case AXE, SWORD, KATANA -> 40; // Swordsmanship
            case PICKAXE, STAFF -> 41; // Mace Fighting
            case SPEAR, KRYSS -> 42; // Fencing
            case BOW, CROSSBOW -> 31; // Archery
            case WRESTLING -> 43;
        };
    }
}
