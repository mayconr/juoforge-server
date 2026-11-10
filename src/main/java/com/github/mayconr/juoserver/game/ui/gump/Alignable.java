package com.github.mayconr.juoserver.game.ui.gump;

/** Alignment within the area assigned by the parent layout. */
public interface Alignable extends UIElement {
    /** Centers rows horizontally, columns vertically, and other elements on both axes. */
    Alignable center();

    /** Aligns horizontally to the left, preserving vertical alignment. */
    Alignable left();

    /** Aligns horizontally to the right, preserving vertical alignment. */
    Alignable right();

    /** Aligns vertically to the top, preserving horizontal alignment. */
    Alignable top();

    /** Aligns vertically to the bottom, preserving horizontal alignment. */
    Alignable down();
}
