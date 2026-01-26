package com.github.mayconr.juoserver.game.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ItemType {
    WEAPON(1),
    CONTAINER(2),
    CLOTHING(3),
    MOUNT(4),
    OTHER(5);

    private final int code;

    public static ItemType byCode(int code) {
        for (ItemType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        return ItemType.OTHER;
    }
}
