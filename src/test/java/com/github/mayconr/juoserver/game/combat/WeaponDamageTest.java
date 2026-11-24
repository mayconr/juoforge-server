package com.github.mayconr.juoserver.game.combat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mayconr.juoserver.game.GamePlaySettings.Player.Status;
import com.github.mayconr.juoserver.game.combat.flow.execution.CombatType;
import com.github.mayconr.juoserver.game.damage.shared.CalculateDamageContext;
import com.github.mayconr.juoserver.game.damage.shared.CalculateDamageContext.DamageRequest;
import com.github.mayconr.juoserver.game.damage.shared.CalculateDamageStep;
import com.github.mayconr.juoserver.game.item.template.ItemTemplate;
import com.github.mayconr.juoserver.game.model.DamageComponent;
import com.github.mayconr.juoserver.game.model.DamageType;
import com.github.mayconr.juoserver.game.model.ItemLocationType;
import com.github.mayconr.juoserver.game.model.Layer;
import com.github.mayconr.juoserver.game.model.SkillContainer;
import com.github.mayconr.juoserver.game.model.SkillValue;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.infrastructure.storage.WorldStorage;
import com.github.mayconr.juoserver.infrastructure.storage.item.Items;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WeaponDamageTest {
    @ParameterizedTest
    @CsvSource({"MELEE, 0, 20", "MELEE, 50, 23", "RANGED, 100, 26", "WRESTLING, 120, 26", "SPELL, 100, 20"})
    void appliesEffectiveTacticsToPhysicalAttacks(CombatType type, double effectiveTactics, int expected) {
        var tactics = SkillValue.of(SkillValue.TACTICS_ID, 0, 120);
        tactics.applyModifier(effectiveTactics);
        var attacker = mock(UOMobile.class);
        when(attacker.getStrength()).thenReturn(200);
        when(attacker.getSkills()).thenReturn(new SkillContainer(List.of(tactics)));
        var context = mock(CalculateDamageContext.class);
        when(context.damageRequest()).thenReturn(new DamageRequest(type, null, attacker, mock(UOMobile.class)));

        new CalculateDamageStep<>().execute(context);

        verify(context).addDamage(new DamageComponent(DamageType.PHYSICAL, expected));
    }

    @ParameterizedTest
    @CsvSource({"0, 0, 20, 20", "12, 12, 22, 22", "0, 1, 7, 7", "10, 15, 20, 25"})
    void reportsTheRangeOfDamageFromTheReloadedItemOrItsTemplate(
            int min, int max, int expectedMin, int expectedMax)
            throws Exception {
        var template = new ObjectMapper().readValue("""
                {"name":"katana","weapon":{"style":"KATANA","baseDamage":{"min":10,"max":10},"radius":1}}
                """, ItemTemplate.class);
        var data = template.toData(UOItem.OBJECTS_MIN_SERIAL_ID);
        data.setLocationType(ItemLocationType.ORPHAN);
        data.setMinDamage(min);
        data.setMaxDamage(max);
        var weapon = new UOItem(new UOItem(data, template).toData(), template);
        var attacker = mock(UOMobile.class);
        when(attacker.getStrength()).thenReturn(50);
        when(attacker.getSkills()).thenReturn(new SkillContainer(List.of(SkillValue.of(SkillValue.TACTICS_ID, 100, 100))));
        when(attacker.getEquippedItems()).thenReturn(Map.of(Layer.ONE_HANDED, weapon.getSerialId()));
        var items = mock(Items.class);
        when(items.get(weapon.getSerialId())).thenReturn(Optional.of(weapon));
        var storage = mock(WorldStorage.class);
        when(storage.items()).thenReturn(items);
        WorldCombat combat = new CombatModuleImpl(new Status(), storage);

        var context = mock(CalculateDamageContext.class);
        when(context.damageRequest()).thenReturn(
                new DamageRequest(CombatType.MELEE, weapon, attacker, mock(UOMobile.class)));

        var range = combat.getEquippedDamageRange(attacker);
        assertEquals(new DamageRange(expectedMin, expectedMax), range);
        new CalculateDamageStep<>().execute(context);

        var result = ArgumentCaptor.forClass(DamageComponent.class);
        verify(context).addDamage(result.capture());
        int damage = result.getValue().damage();
        assertTrue(damage >= range.min() && damage <= range.max());
    }
}
