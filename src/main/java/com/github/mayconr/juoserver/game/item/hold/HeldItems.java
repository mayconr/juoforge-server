package com.github.mayconr.juoserver.game.item.hold;

import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.UOPlayer;

import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Items lifted by players and not yet dropped or equipped. A player holds at most one item and an item is
 * held by at most one player; both rules are enforced atomically because each player's requests run on its
 * own network thread.
 */
public final class HeldItems {
    private final Map<Integer, HeldItem> itemByPlayer = new ConcurrentHashMap<>();
    private final Map<Integer, Integer> playerByItem = new ConcurrentHashMap<>();

    /** Records that the player holds the item; returns false if either is already taken. */
    public synchronized boolean hold(UOPlayer player, UOItem item) {
        int playerSerial = player.getSerialId();
        int itemSerial = item.getSerialId();
        if (playerByItem.putIfAbsent(itemSerial, playerSerial) != null) {
            return false;
        }
        if (itemByPlayer.putIfAbsent(playerSerial, HeldItem.capture(item)) != null) {
            playerByItem.remove(itemSerial, playerSerial);
            return false;
        }
        return true;
    }

    /** Whether the player holds any item. */
    public boolean isHoldingAny(UOPlayer player) {
        return itemByPlayer.containsKey(player.getSerialId());
    }

    /** Whether any player currently holds this item. */
    public boolean isHeld(UOItem item) {
        return playerByItem.containsKey(item.getSerialId());
    }

    /** Whether the player holds the item with the given serial. */
    public boolean isHolding(UOPlayer player, int itemSerial) {
        return get(player).map(held -> held.item().getSerialId() == itemSerial).orElse(false);
    }

    /** The player's held item and the origin saved before it was lifted. */
    public Optional<HeldItem> get(UOPlayer player) {
        return Optional.ofNullable(itemByPlayer.get(player.getSerialId()));
    }

    /** Ends the player's hold, returning the serial of the item that was held. */
    public synchronized OptionalInt release(UOPlayer player) {
        int playerSerial = player.getSerialId();
        var held = itemByPlayer.remove(playerSerial);
        if (held == null) {
            return OptionalInt.empty();
        }
        int itemSerial = held.item().getSerialId();
        playerByItem.remove(itemSerial, playerSerial);
        return OptionalInt.of(itemSerial);
    }
}
