package com.github.mayconr.juoserver.game.ui.gump;

/** Vertical placement of natural content within available space. */
enum VerticalAlignment {
    TOP, CENTER, DOWN;

    int offset(int available, int content) {
        int remaining = Math.max(0, available - content);
        return switch (this) {
            case TOP -> 0;
            case CENTER -> remaining / 2;
            case DOWN -> remaining;
        };
    }
}
