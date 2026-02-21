package com.github.mayconr.juoserver.game.wallet;

import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.world.World;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PhisycalGoldWallet implements Wallet {

    private final World world;

    @Override
    public int getBalance(UOMobile mobile) {
        return world.getItemsInContainer(mobile.getBackpack(), item -> item.getName().equals("gold_coin"))
                .stream()
                .map(UOItem::getAmount)
                .reduce(0, Integer::sum);
    }

    @Override
    public boolean withdraw(UOMobile mobile, int amount) {
        return world.consumeItem(mobile.getBackpack(), "gold_coin", amount, true).success();
    }

    @Override
    public void deposit(UOMobile mobile, int amount) {

    }

    @Override
    public boolean isGold(UOItem item) {
        return "gold_coin".equals(item.getName());
    }
}
