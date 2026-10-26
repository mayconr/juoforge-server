package com.github.mayconr.juoserver.game.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mayconr.juoserver.game.item.template.ItemTemplate;
import com.github.mayconr.juoserver.game.model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ItemMovabilityTest {
    @ParameterizedTest
    @CsvSource({"missing,true", "null,true", "true,true", "false,false"})
    void templateDefaultsToMovableAndPreservesExplicitValues(String value, boolean expected) throws Exception {
        String property = value.equals("missing") ? "" : ", \"movable\": " + value;
        String json = "{\"name\":\"test\",\"modelId\":1" + property + "}";
        var mapper = new ObjectMapper();
        var template = mapper.readValue(json, ItemTemplate.class);
        assertEquals(expected, template.movable());
        var item = new UOItem(template.toData(0x40000001), template);
        assertEquals(expected, item.isMovable());
        assertEquals(expected, item.toData().isMovable());
        assertEquals(expected, mapper.readValue(mapper.writeValueAsString(template), ItemTemplate.class).movable());
    }

    @Test
    void programmaticItemsDefaultToMovableButMayBeExplicitlyImmovable() {
        var data = new UOItemData();
        data.setLocationType(ItemLocationType.ORPHAN);
        data.setFlags(List.of());
        assertTrue(new UOItem(data, null).isMovable());
        data.setMovable(false);
        assertFalse(new UOItem(data, null).isMovable());
    }
}
