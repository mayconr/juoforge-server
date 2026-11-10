package com.github.mayconr.juoserver.game.ui.gump;

/** A UI element that can display a text tooltip or an existing object's properties. */
public interface Tooltipped extends UIElement {
    /** Uses the ~1_NOTHING~ cliloc to display the supplied text. */
    Tooltipped tooltip(String text);

    /** Uses a client-localized cliloc and optional substitution arguments. */
    Tooltipped tooltip(int clilocId, String... arguments);

    /** Uses an existing object's property list. The last tooltip configuration wins. */
    Tooltipped itemProperty(int serialId);
}
