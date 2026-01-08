package com.github.mayconr.juoserver.game.server;

import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.mayconr.juoserver.game.packet.*;
import com.github.mayconr.juoserver.game.packet.RequestWarMode;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

public class UOProtocolDecoder extends ByteToMessageDecoder {

    private static final Logger LOGGER = LoggerFactory.getLogger(UOProtocolDecoder.class);
    private final Map<Integer, Class<? extends AbstractPacket>> packetsClass = new HashMap<>();
    private boolean hasSeed;

    public UOProtocolDecoder() {
        packetsClass.put(GameServerLogin.CODE, GameServerLogin.class);
        packetsClass.put(PingPong.CODE, PingPong.class);
        packetsClass.put(LoginCharacter.CODE, LoginCharacter.class);
        packetsClass.put(DeleteCharacter.CODE, DeleteCharacter.class);
        packetsClass.put(CreateCharacter.CODE, CreateCharacter.class);
        packetsClass.put(ClientVersion.CODE, ClientVersion.class);
        packetsClass.put(MoveRequest.CODE, MoveRequest.class);
        packetsClass.put(DoubleClick.CODE, DoubleClick.class);
        packetsClass.put(UnicodeSpeachRequest.CODE, UnicodeSpeachRequest.class);
        packetsClass.put(MegaCliloc.CODE, MegaCliloc.class);
        packetsClass.put(GeneralInformation.CODE, GeneralInformation.class);
        packetsClass.put(LookRequest.CODE, LookRequest.class);
        packetsClass.put(PickUpItem.CODE, PickUpItem.class);
        packetsClass.put(DropItem.CODE, DropItem.class);
        packetsClass.put(EquipItemRequest.CODE, EquipItemRequest.class);
        packetsClass.put(Target.CODE, Target.class);
        packetsClass.put(GetPlayerStatus.CODE, GetPlayerStatus.class);
        packetsClass.put(RequestHelp.CODE, RequestHelp.class);
        packetsClass.put(RequestWarMode.CODE, RequestWarMode.class);
        packetsClass.put(AttackRequest.CODE, AttackRequest.class);
        packetsClass.put(GumpSelection.CODE, GumpSelection.class);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf buf, List<Object> out)
            throws Exception {
        if (!hasSeed) {
            var seed = new LoginSeedPacket(buf);
            LOGGER.info("Seed received from address {}", seed.getAddress());
            this.hasSeed = true;
        }

        boolean hasUnknownPacket = false;
        while (buf.readableBytes() > 0 && !hasUnknownPacket) {
            var code = buf.getByte(buf.readerIndex());
            var hexCode = HexFormat.of().formatHex(new byte[] {code}).toUpperCase();

            var packetClass = packetsClass.get((int) code);
            if (packetClass != null) {
                out.add(packetClass.getConstructor(ByteBuf.class).newInstance(buf));
                LOGGER.debug("Packet received [0x{} - {}]", hexCode, packetClass.getSimpleName());
            } else {
                hasUnknownPacket = true;
                LOGGER.info(
                        "Unknown packet [0x{}] is not possible to decode remaining data.", hexCode);

                byte[] remainingData = new byte[buf.readableBytes()];
                buf.readBytes(remainingData);
                LOGGER.debug(
                        "Unknown packet data [{}]",
                        HexFormat.of().formatHex(remainingData).toUpperCase());
            }
        }
    }
}
