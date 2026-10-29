package com.github.mayconr.juoserver.game.mobile;

import com.github.mayconr.juoserver.game.GamePlaySettings;
import com.github.mayconr.juoserver.game.mobile.flow.setattribute.*;
import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.game.ui.UIModule;
import com.github.mayconr.juoserver.game.world.context.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SetMobileStatsFlowTest {
    @ParameterizedTest
    @CsvSource({"str, true", "dex, true", "int, true", "str, false", "dex, false", "int, false"})
    void setsRequestedAttributeBeforeNotifyingPlayer(String attribute, boolean player) {
        var data = new UOMobileData();
        data.setSerialId(42);
        data.setStrength(10);
        data.setDexterity(20);
        data.setIntelligence(30);
        UOMobile mobile = player ? new UOPlayer(data) : new UONpc(data);
        var ui = mock(UIModule.class);
        var registry = new DefaultFlowRegistry();
        registry.register("set-attribute", SetAttributeFlowDefinition.build(ui), SetAttributeContext.class);
        var module = new MobileModuleImpl(null, null, null, null, new GamePlaySettings());
        module.initialize(new DefaultModuleContext(new DefaultFlowFacade(registry)));
        if (player) {
            doAnswer(invocation -> {
                assertStats(mobile, attribute);
                assertEquals(attribute.equals("str") ? 65 : 26, module.calculateMaxWeight(mobile));
                return null;
            }).when(ui).sendStatusGump((UOPlayer) mobile, 42);
        }

        module.setAttribute(mobile, MobileAttribute.valueOf(attribute.toUpperCase(java.util.Locale.ROOT)), 75);

        assertStats(mobile, attribute);
        if (player) verify(ui).sendStatusGump((UOPlayer) mobile, 42);
        else verifyNoInteractions(ui);
    }

    private void assertStats(UOMobile mobile, String attribute) {
        assertEquals(attribute.equals("str") ? 75 : 10, mobile.getStrength());
        assertEquals(attribute.equals("dex") ? 75 : 20, mobile.getDexterity());
        assertEquals(attribute.equals("int") ? 75 : 30, mobile.getIntelligence());
    }
}
