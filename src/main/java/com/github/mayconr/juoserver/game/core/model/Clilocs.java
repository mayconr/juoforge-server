package com.github.mayconr.juoserver.game.core.model;

public enum Clilocs {
    /** ~1_PREFIX~ ~2_NAME~ ~3_SUFFIX~ */
    PREFIX_NAME_SUFFIX(0x1005BD),

    /** ~1_NUMBER~ ~2_ITEMNAME~ */
    ITEM_NAME(1050039);

    private final int code;

    Clilocs(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
