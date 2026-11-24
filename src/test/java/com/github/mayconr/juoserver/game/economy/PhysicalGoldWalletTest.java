package com.github.mayconr.juoserver.game.economy;

import com.github.mayconr.juoserver.DefaultWorldConfigurator;
import com.github.mayconr.juoserver.game.GamePlaySettings;
import com.github.mayconr.juoserver.game.GamePlaySettings.Economy;
import com.github.mayconr.juoserver.game.container.WorldContainer;
import com.github.mayconr.juoserver.game.model.ConsumeResult;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.world.World;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PhysicalGoldWalletTest {
    @Test
    void configuredCoinIsUsedForRecognitionBalanceAndWithdrawal() {
        var configuration = new DefaultWorldConfigurator();
        configuration.gameplaySettings(new GamePlaySettings().withEconomy(new Economy("silver_coin")));
        var world = mock(World.class);
        var containers = mock(WorldContainer.class);
        var mobile = mock(UOMobile.class);
        var silver = mock(UOItem.class);
        var gold = mock(UOItem.class);
        int backpack = 0x40000001;

        when(world.container()).thenReturn(containers);
        when(mobile.getBackpack()).thenReturn(backpack);
        when(silver.getName()).thenReturn("silver_coin");
        when(silver.getAmount()).thenReturn(12);
        when(gold.getName()).thenReturn("gold_coin");
        when(containers.getItemsInContainer(eq(backpack), any())).thenAnswer(invocation -> {
            Predicate<UOItem> filter = invocation.getArgument(1);
            return List.of(silver, gold).stream().filter(filter).toList();
        });
        when(containers.consumeItem(backpack, "silver_coin", 5, true)).thenReturn(new ConsumeResult(true, 0));

        var wallet = configuration.wallet().apply(world);

        assertTrue(wallet.isGold(silver));
        assertFalse(wallet.isGold(gold));
        assertEquals(12, wallet.getBalance(mobile));
        assertTrue(wallet.withdraw(mobile, 5));
    }
}
