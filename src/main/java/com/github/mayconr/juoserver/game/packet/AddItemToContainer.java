package com.github.mayconr.juoserver.game.packet;

import com.github.mayconr.juoserver.game.core.model.Container;
import com.github.mayconr.juoserver.game.core.model.UOItem;
import com.github.mayconr.juoserver.game.core.model.UOMobile;
import com.github.mayconr.juoserver.game.server.AbstractPacket;

import io.netty.buffer.ByteBuf;
import lombok.Getter;

@Getter
public class AddItemToContainer extends AbstractPacket {

    public static final int CODE = (byte) 0x25;

    private final Container container;
    private final int containerSerialId;
    private final UOItem itemToBeAdded;

    public AddItemToContainer(Container container, UOItem itemToBeAdded) {
        super(CODE, 21);
        this.container = container;
        this.containerSerialId = container.getSerialId();
        this.itemToBeAdded = itemToBeAdded;
    }

    public AddItemToContainer(UOMobile mobile, UOItem itemToBeAdded) {
        super(CODE, 21);
        this.container = mobile;
        this.containerSerialId = mobile.getBackpack().getSerialId();
        this.itemToBeAdded = itemToBeAdded;
    }

    @Override
    public void writesTo(ByteBuf buf) {
        buf.writeByte(CODE);
        buf.writeInt(itemToBeAdded.getSerialId());
        buf.writeShort(itemToBeAdded.getModelId());
        buf.writeByte(0); // unknown
        buf.writeShort(itemToBeAdded.getAmount());
        buf.writeShort(itemToBeAdded.getX());
        buf.writeShort(itemToBeAdded.getY());
        buf.writeByte(itemToBeAdded.getZ());
        buf.writeInt(containerSerialId);
        buf.writeShort(itemToBeAdded.getHue());
    }
}
