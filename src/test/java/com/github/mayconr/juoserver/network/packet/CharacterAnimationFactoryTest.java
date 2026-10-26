package com.github.mayconr.juoserver.network.packet;

import com.github.mayconr.juoserver.game.item.template.ItemTemplate;
import com.github.mayconr.juoserver.game.model.*;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class CharacterAnimationFactoryTest {
    private UOMobileData data(int body) {
        var data = new UOMobileData();
        data.setSerialId(42);
        data.setModelId(body);
        return data;
    }

    private void assertAction(CharacterAnimation animation, int expected) {
        var buf = Unpooled.buffer();
        try {
            animation.writesTo(buf);
            assertEquals(0x6E, buf.getUnsignedByte(0));
            assertEquals(42, buf.getInt(1));
            assertEquals(expected, buf.getUnsignedShort(5));
        } finally {
            buf.release();
        }
    }

    @ParameterizedTest
    @CsvSource({"WRESTLING, 9", "SWORD, 11", "SPEAR, 14", "BOW, 18", "CROSSBOW, 19"})
    void playersUseWeaponRules(WeaponStyle style, int expected) {
        var player = new UOPlayer(data(400));
        assertAction(CharacterAnimationFactory.weapon(player,
                new ItemTemplate.Weapon(style, null, 1), 5), expected);
    }

    @ParameterizedTest
    @CsvSource({"WRESTLING, 26", "SWORD, 26", "BOW, 27", "CROSSBOW, 28"})
    void mountedPlayersUseMountedWeaponRules(WeaponStyle style, int expected) {
        var player = new UOPlayer(data(400));
        player.getEquippedItems().put(Layer.MOUNT, 123);
        assertAction(CharacterAnimationFactory.weapon(player,
                new ItemTemplate.Weapon(style, null, 1), 5), expected);
    }

    @ParameterizedTest
    @CsvSource({"WRESTLING, 9", "SWORD, 11", "SPEAR, 14", "BOW, 18", "CROSSBOW, 19"})
    void npcsUseTheSameWeaponRules(WeaponStyle style, int expected) {
        var skeleton = new UONpc(data(57));
        assertAction(CharacterAnimationFactory.weapon(skeleton,
                new ItemTemplate.Weapon(style, null, 1), 5), expected);
    }

    @Test
    void wrestlingUsesTheSameRuleForAllMobiles() {
        assertAction(CharacterAnimationFactory.wrestling(new UOPlayer(data(400)), 5), 9);
        assertAction(CharacterAnimationFactory.wrestling(new UONpc(data(57)), 5), 9);
        assertAction(CharacterAnimationFactory.wrestling(new UONpc(data(26)), 5), 9);
        assertAction(CharacterAnimationFactory.wrestling(new UONpc(data(200)), 5), 9);
    }

    @ParameterizedTest
    @CsvSource({"400", "401", "605", "606", "666", "667"})
    void humanoidNpcsUseHumanoidWeaponActions(int body) {
        var npc = new UONpc(data(body));
        assertAction(CharacterAnimationFactory.weapon(npc,
                new ItemTemplate.Weapon(WeaponStyle.SWORD, null, 1), 5), 11);
    }
}
