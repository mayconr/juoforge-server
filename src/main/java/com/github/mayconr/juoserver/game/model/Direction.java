package com.github.mayconr.juoserver.game.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@Getter
@RequiredArgsConstructor
public enum Direction {
    NORTH(0, 0, -1),
    NORTHEAST(1, 1, -1),
    EAST(2, 1, 0),
    SOUTHEAST(3, 1, 1),
    SOUTH(4, 0, 1),
    SOUTHWEST(5, -1, 1),
    WEST(6, -1, 0),
    NORTHWEST(7, -1, -1);

    private final int code;
    private final int dx;
    private final int dy;

    public int getCode() {
        return ordinal();
    }

    public static Direction fromDelta(int dx, int dy) {
        int ndx = Integer.signum(dx);
        int ndy = Integer.signum(dy);

        for (Direction dir : values()) {
            if (dir.getDx() == ndx && dir.getDy() == ndy) {
                return dir;
            }
        }
        throw new IllegalArgumentException("Invalid Direction");
    }

    public static Direction fromCode(int code) {
        for (Direction d : values()) {
            if (d.code == code) {
                return d;
            }
        }
        return SOUTH;
    }

}
