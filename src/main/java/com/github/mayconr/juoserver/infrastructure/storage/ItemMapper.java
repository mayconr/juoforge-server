package com.github.mayconr.juoserver.infrastructure.storage;

import com.github.mayconr.juoserver.game.item.template.ItemTemplate;
import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.infrastructure.template.TemplateRegistry;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class ItemMapper {

    private final TemplateRegistry<String, ItemTemplate> itemTemplateByName;

    public UOItemData mapToData(UOItem uoItem) {
        return uoItem.toData();
    }

    public UOItem mapToItem(UOItemData uoItemData) {
        if (uoItemData == null) {
            return null;
        }
        final var template = Optional.ofNullable(uoItemData.getTemplate())
                .flatMap(itemTemplateByName::getFisrt)
                .orElse(null); // TODO ajustar para nao suportar null

        var flags = uoItemData.getFlags() == null ? new ArrayList<>() : uoItemData.getFlags();
        if (flags.contains(ItemFlag.CORPSE)) {
            return new UOCorpse(uoItemData, template);
        }
        if (flags.contains(ItemFlag.CONTAINER)) {
            return new UOContainer(uoItemData, template);
        }
        return new UOItem(uoItemData, template);
    }

    public List<UOItemData> mapToData(List<UOItem> uoItems) {
        return uoItems.stream().map(this::mapToData).toList();
    }

    public List<UOItem> mapToItem(Collection<UOItemData> uoItems) {
        return uoItems.stream().map(this::mapToItem).toList();
    }
}
