package com.github.mayconr.juoserver.infrastructure.storage;

import com.github.mayconr.juoserver.game.model.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ItemMapper {

    public static UOItemData mapToData(UOItem uoItem) {
        return uoItem.toData();
    }

    public static UOItem mapToItem(UOItemData uoItemData) {
        if (uoItemData == null) {
            return null;
        }
        var flags = uoItemData.getFlags() == null ? new ArrayList<>() : uoItemData.getFlags();
        if (flags.contains(ItemFlag.CORPSE)) {
            return new UOCorpse(uoItemData);
        }
        if (flags.contains(ItemFlag.CONTAINER)) {
            return new UOContainer(uoItemData);
        }
        return new UOItem(uoItemData);
    }

    public static List<UOItemData> mapToData(List<UOItem> uoItems) {
        return uoItems.stream().map(ItemMapper::mapToData).toList();
    }

    public static List<UOItem> mapToItem(Collection<UOItemData> uoItems) {
        return uoItems.stream().map(ItemMapper::mapToItem).toList();
    }
}
