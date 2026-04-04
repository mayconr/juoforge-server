package com.github.mayconr.shard.storage;

import com.github.mayconr.juoserver.game.model.UOItemData;

import java.util.List;
import java.util.UUID;

public interface ItemMapper {

    Integer findNextItemSerial();

    int updateItemSerial(long serial);

    UOItemData findItemBySerialId(int serialId);

    List<UOItemData> findAllEquippedItems(int ownerSerialId);

    List<UOItemData> findAllGroundItems();

    List<UOItemData> findAllContainerItems(int containerSerialId);

    void upsert(UOItemData data);

    int upsertItemState(UOItemData data);

    int deleteById(UUID id);

}
