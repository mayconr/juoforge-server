package com.github.mayconr.juoserver.game;

import com.github.mayconr.juoserver.game.model.*;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class TestFactory {

    public static UOMobile createTestMobile(List<SkillValue> skills) {
        final var mobile = createTestMobile();
        mobile.setSkills(new SkillContainer(skills));
        return mobile;
    }

    public static UOMobile createTestMobile() {
        return new UOMobile(
                UUID.randomUUID(),
                1,                  // serialId
                0,                  // modelId
                0, 0, 0,             // x, y, z
                "TestMobile",        // name
                "Test Mobile",       // displayName
                new HashMap<>(),     // attrMap
                Direction.SOUTH,     // direction
                0,                  // hue
                CharacterStatus.NORMAL,
                Notoriety.INNOCENT,
                false,              // running
                Race.HUMAN,
                Gender.HUMAN_MALE,

                // stats vitais
                100, 100,            // hp / maxHp
                50,                  // str
                50,                  // dex
                50,                  // int
                100, 100,            // stamina
                100, 100,            // mana

                // carga
                0,                   // gold
                0, 400,              // weight / maxWeight
                225,                 // statCap
                0, 5,                // followers

                // resists
                0, 70,               // physical
                0, 70,               // fire
                0, 70,               // cold
                0, 70,               // poison
                0, 70,               // energy

                // misc
                0,                   // luck
                1, 4,                // damage min/max
                0,                   // tithing

                // modifiers
                0, 45,               // dci
                0,                   // hci
                0,                   // ssi
                0,                   // wdi
                0,                   // lrc
                0,                   // sdi
                0,                   // reflect
                0,                   // enhance potions
                0,                   // fcr
                0,                   // fc
                0                    // lmc
        );
    }

}
