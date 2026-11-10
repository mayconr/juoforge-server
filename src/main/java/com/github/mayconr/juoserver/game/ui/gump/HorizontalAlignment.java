package com.github.mayconr.juoserver.game.ui.gump;

/** Horizontal placement of natural content within available space. */
enum HorizontalAlignment {
    LEFT, CENTER, RIGHT;

    int offset(int available, int content) {
        int remaining = Math.max(0, available - content);
        return switch (this) {
            case LEFT -> 0;
            case CENTER -> remaining / 2;
            case RIGHT -> remaining;
        };
    }
}
