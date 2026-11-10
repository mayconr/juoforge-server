package com.github.mayconr.juoserver.game.ui.gump;

interface Sized {
    int getWidth();

    int getHeight();

    /** Natural dimensions, independent of space assigned during a previous layout. */
    default int getPreferredWidth() { return getWidth(); }

    default int getPreferredHeight() { return getHeight(); }
}
