package com.github.mayconr.juoserver.game.ui.gump;

import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.infrastructure.datafile.ItemTileInfo;
import com.github.mayconr.juoserver.infrastructure.datafile.UOFileReader;

import java.util.Objects;
import java.util.Optional;

/** Item icon that represents a {@link UOItem}, rendering its model ID and hue. */
public class UOItemIcon extends ItemIcon {
    /** Layout size reserved by default for the item art. */
    public static final int DEFAULT_SIZE = 50;

    /** Reserves {@value #DEFAULT_SIZE} x {@value #DEFAULT_SIZE} px for the item art in the layout. */
    public UOItemIcon(UOItem item) {
        this(item, DEFAULT_SIZE, DEFAULT_SIZE);
    }

    /** Resolves art dimensions by model ID, falling back to the default size on each axis. */
    public UOItemIcon(UOItem item, UOFileReader reader) {
        this(item, Objects.requireNonNull(reader, "File reader is required")
                .getItemTileInfo(Objects.requireNonNull(item, "Item is required").getModelId()));
    }

    private UOItemIcon(UOItem item, Optional<ItemTileInfo> info) {
        this(
                item,
                info.map(ItemTileInfo::width).filter(width -> width > 0).orElse(DEFAULT_SIZE),
                info.map(ItemTileInfo::height).filter(height -> height > 0).orElse(DEFAULT_SIZE)
        );
    }

    /** Actual art dimensions used for layout only; tilepic does not resize client artwork. */
    public UOItemIcon(UOItem item, int width, int height) {
        super(Objects.requireNonNull(item, "Item is required").getModelId(), width, height);
        hue(item.getHue());
    }
}
