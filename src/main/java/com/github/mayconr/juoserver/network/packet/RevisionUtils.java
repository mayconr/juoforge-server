package com.github.mayconr.juoserver.network.packet;

import com.github.mayconr.juoserver.game.model.Clilocs;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.UOMobile;

public class RevisionUtils {
    static int getItemClilocID(UOItem it) {
        if (it.getModelId() < 0x4000) {
            return 1020000 + it.getModelId();
        } else {
            return 1078872 + it.getModelId();
        }
    }

    static int mobileRevisionHashCode(UOMobile mobile) {
        int hash = addHashTo(0, Clilocs.PREFIX_NAME_SUFFIX.getCode());
        return addHashTo(hash, mobile.getName().hashCode());
    }

    static int itemRevisionHashCode(UOItem item) {
        int hash = addHashTo(0, Clilocs.ITEM_NAME.getCode());
        return addHashTo(hash, getItemClilocID(item));
    }

    private static int addHashTo(int originalHash, int value) {
        originalHash ^= value & 0x3FFFFFF;
        originalHash ^= (value >> 26) & 0x3F;
        return originalHash;
    }
}
