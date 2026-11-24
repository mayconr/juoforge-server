package com.github.mayconr.juoserver.game.wallet;

import com.github.mayconr.juoserver.game.GamePlaySettings;
import com.github.mayconr.juoserver.game.GamePlaySettings.Economy;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.world.World;

import java.util.Objects;

public class PhisycalGoldWallet implements Wallet {

    private final World world;
    private final String goldCoinItem;

    public PhisycalGoldWallet(World world) {
        this(world, new GamePlaySettings().economy());
    }

    public PhisycalGoldWallet(World world, Economy economy) {
        this.world = world;
        this.goldCoinItem = Objects.requireNonNull(economy.goldCoinItem(), "Economy gold coin item is required");
    }

    @Override
    public int getBalance(UOMobile mobile) {
        var backpack = mobile.getBackpack();
        if (backpack == null) {
            return 0;
        }
        return world.container().getItemsInContainer(backpack, this::isGold)
                .stream()
                .map(UOItem::getAmount)
                .reduce(0, Integer::sum);
    }

    @Override
    public boolean withdraw(UOMobile mobile, int amount) {
        return world.container().consumeItem(mobile.getBackpack(), goldCoinItem, amount, true).success();
    }

    @Override
    public void deposit(UOMobile mobile, int amount) {

    }

    @Override
    public boolean isGold(UOItem item) {
        return goldCoinItem.equals(item.getName());
    }
}
