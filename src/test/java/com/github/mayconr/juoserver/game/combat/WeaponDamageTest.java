package com.github.mayconr.juoserver.game.combat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mayconr.juoserver.game.combat.flow.execution.CombatType;
import com.github.mayconr.juoserver.game.damage.shared.CalculateDamageContext;
import com.github.mayconr.juoserver.game.damage.shared.CalculateDamageContext.DamageRequest;
import com.github.mayconr.juoserver.game.damage.shared.CalculateDamageStep;
import com.github.mayconr.juoserver.game.item.template.ItemTemplate;
import com.github.mayconr.juoserver.game.model.DamageComponent;
import com.github.mayconr.juoserver.game.model.DamageType;
import com.github.mayconr.juoserver.game.model.ItemLocationType;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.UOMobile;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WeaponDamageTest {
    @ParameterizedTest
    @CsvSource({"0, 0, 10", "12, 12, 12", "0, 1, 0"})
    void resolvesDamageFromTheReloadedItemBeforeFallingBackToItsTemplate(int min, int max, int expected)
            throws Exception {
        var template = new ObjectMapper().readValue("""
                {"name":"katana","weapon":{"style":"KATANA","baseDamage":{"min":10,"max":10},"radius":1}}
                """, ItemTemplate.class);
        var data = template.toData(UOItem.OBJECTS_MIN_SERIAL_ID);
        data.setLocationType(ItemLocationType.ORPHAN);
        data.setMinDamage(min);
        data.setMaxDamage(max);
        var weapon = new UOItem(new UOItem(data, template).toData(), template);

        var context = mock(CalculateDamageContext.class);
        when(context.damageRequest()).thenReturn(
                new DamageRequest(CombatType.MELEE, weapon, mock(UOMobile.class), mock(UOMobile.class)));

        new CalculateDamageStep<>().execute(context);

        verify(context).addDamage(new DamageComponent(DamageType.PHYSICAL, expected));
    }
}
