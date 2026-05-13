package com.github.mayconr.juoserver.network.packet;

import com.github.mayconr.juoserver.game.model.Container;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.VendorSessionItem;
import com.github.mayconr.juoserver.infrastructure.server.AbstractPacket;
import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class AddMultipleItemsToContainer extends AbstractPacket {
    public static final int CODE = (byte) 0x3C;
    private final Container container;
    private final Collection<ContainerItem> items;

    public static AddMultipleItemsToContainer ofUOItem(Container container, Collection<UOItem> items) {
        List<ContainerItem> containerItems = new ArrayList<>(items.size());
        for (UOItem item : items) {
            containerItems.add(new UOContainerItem(item));
        }
        return new AddMultipleItemsToContainer(container, containerItems);
    }

    public static AddMultipleItemsToContainer ofStockItem(Container container, Collection<VendorSessionItem> items) {
        List<ContainerItem> containerItems = new ArrayList<>(items.size());
        for (VendorSessionItem item : items) {
            containerItems.add(new StockContainerItem(item));
        }
        return new AddMultipleItemsToContainer(container, containerItems);
    }

    private AddMultipleItemsToContainer(Container container, Collection<ContainerItem> items) {
        super(CODE, computeLength(items.size()));
        this.container = container;
        this.items = items;
    }

    private static int computeLength(int itemCount) {
        return 5 + itemCount * 20;
    }

    @Override
    public void writesTo(ByteBuf buf) {
        buf.writeByte(CODE);
        buf.writeShort(getLength());
        buf.writeShort(items.size());
        for (ContainerItem item : items) {
            buf.writeInt(item.getSerialId());
            buf.writeShort(item.getModelId());
            buf.writeByte(0); // unknown
            buf.writeShort(item.getAmount());
            System.out.println(item.getX() + " " + item.getY() + " " + item.getZ());
            buf.writeShort(item.getX());
            buf.writeShort(item.getY());
            buf.writeByte(item.getZ());
            buf.writeInt(container.getSerialId());
            buf.writeShort(item.getHue());
        }
    }

    private interface ContainerItem {
        int getAmount();
        int getX();
        int getY();
        int getZ();
        int getSerialId();
        int getModelId();
        int getHue();
    }

    private record UOContainerItem(UOItem item) implements ContainerItem {

            @Override
            public int getAmount() {
                return item.getAmount();
            }

            @Override
            public int getX() {
                return item.getX();
            }

            @Override
            public int getY() {
                return item.getY();
            }

            @Override
            public int getZ() {
                return item.getZ();
            }

            @Override
            public int getSerialId() {
                return item.getSerialId();
            }

            @Override
            public int getModelId() {
                return item.getModelId();
            }

            @Override
            public int getHue() {
                return item.getHue();
            }
        }

    private record StockContainerItem(VendorSessionItem item) implements ContainerItem {
            @Override
            public int getAmount() {
                return item.entry().getCurrentStock();
            }

            @Override
            public int getX() {
                return 0;
            }

            @Override
            public int getY() {
                return 0;
            }

            @Override
            public int getZ() {
                return 0;
            }

            @Override
            public int getSerialId() {
                return item.serialId();
            }

            @Override
            public int getModelId() {
                return item.entry().getItemTemplate().modelId();
            }

            @Override
            public int getHue() {
                return item.entry().getItemTemplate().hue();
            }
        }
}
