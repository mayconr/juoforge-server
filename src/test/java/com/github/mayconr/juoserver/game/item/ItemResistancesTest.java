package com.github.mayconr.juoserver.game.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mayconr.juoserver.game.item.template.ItemTemplate;
import com.github.mayconr.juoserver.game.model.ItemResistances;
import com.github.mayconr.juoserver.game.model.ItemLocationType;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.UOItemData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ItemResistancesTest {
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void templateContributionsSurviveItemAndDataJsonRoundTrips() throws Exception {
        var template = mapper.readValue("""
                {"name":"platemail","resistances":
                  {"physical":5,"fire":3,"cold":2,"poison":0,"energy":100}}
                """, ItemTemplate.class);
        var expected = new ItemResistances(5, 3, 2, 0, 100);
        assertEquals(expected, template.resistances());
        var item = new UOItem(template.toData(100), template);
        assertEquals(expected, item.getResistances());
        var data = item.toData();
        data.setPersistentAttrMap(null);
        var saved = mapper.readValue(mapper.writeValueAsString(data), UOItemData.class);
        assertEquals(expected, new UOItem(saved, template).getResistances());
        assertEquals(template, mapper.readValue(mapper.writeValueAsString(template), ItemTemplate.class));
    }

    @Test
    void missingResistancesAndMissingChannelsDefaultToZero() throws Exception {
        var template = mapper.readValue("{\"name\":\"shirt\"}", ItemTemplate.class);
        assertEquals(ItemResistances.NONE, template.resistances());
        assertEquals(ItemResistances.NONE, new UOItem(template.toData(100), template).getResistances());
        var legacy = new UOItemData();
        legacy.setLocationType(ItemLocationType.ORPHAN);
        assertEquals(ItemResistances.NONE, new UOItem(legacy, null).getResistances());
        var partial = mapper.readValue("{\"resistances\":{\"physical\":5}}", ItemTemplate.class);
        assertEquals(new ItemResistances(5, 0, 0, 0, 0), partial.resistances());
    }

    @Test
    void rejectsOutOfRangeValuesInEveryChannel() {
        for (int invalid : new int[]{-1, 101}) {
            assertThrows(IllegalArgumentException.class, () -> new ItemResistances(invalid, 0, 0, 0, 0));
            assertThrows(IllegalArgumentException.class, () -> new ItemResistances(0, invalid, 0, 0, 0));
            assertThrows(IllegalArgumentException.class, () -> new ItemResistances(0, 0, invalid, 0, 0));
            assertThrows(IllegalArgumentException.class, () -> new ItemResistances(0, 0, 0, invalid, 0));
            assertThrows(IllegalArgumentException.class, () -> new ItemResistances(0, 0, 0, 0, invalid));
        }
        assertThrows(Exception.class, () -> mapper.readValue(
                "{\"resistances\":{\"physical\":101}}", ItemTemplate.class));
    }
}
