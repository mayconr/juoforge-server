package com.github.mayconr.juoserver.game.wallet;

import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.UOMobile;

public interface Wallet {
    int getBalance(UOMobile mobile);

    boolean withdraw(UOMobile mobile, int amount);

    void deposit(UOMobile mobile, int amount);

    boolean isGold(UOItem item);
}
