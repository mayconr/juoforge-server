package com.github.mayconr.juoserver.infrastructure.server;

import com.github.mayconr.juoserver.network.packet.*;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;

@Slf4j
public class UOProtocolDecoder extends ByteToMessageDecoder {

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
        packetsClass.put(TooltipRequest.CODE, TooltipRequest.class);
        packetsClass.put(SingleClickRequest.CODE, SingleClickRequest.class);
        packetsClass.put(UnequipItem.CODE, UnequipItem.class);
        packetsClass.put(DropItem.CODE, DropItem.class);
        packetsClass.put(EquipItemRequest.CODE, EquipItemRequest.class);
        packetsClass.put(Target.CODE, Target.class);
        packetsClass.put(GetPlayerStatus.CODE, GetPlayerStatus.class);
        packetsClass.put(RequestHelp.CODE, RequestHelp.class);
        packetsClass.put(RequestWarMode.CODE, RequestWarMode.class);
        packetsClass.put(AttackRequest.CODE, AttackRequest.class);
        packetsClass.put(GumpSelection.CODE, GumpSelection.class);
        packetsClass.put(UseRequest.CODE, UseRequest.class);
        packetsClass.put(ActionRequest.CODE, ActionRequest.class);
        packetsClass.put(SendSkill.CODE, SendSkill.class);
        packetsClass.put(VendorBuyRequest.CODE, VendorBuyRequest.class);
        packetsClass.put(MoveResyncAck.CODE, MoveResyncAck.class);
        packetsClass.put(GeneralInformation.CODE, GeneralInformation.class);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf buf, List<Object> out)
            throws Exception {
        if (!hasSeed) {
            var seed = new LoginSeedPacket(buf);
            log.info("Seed received from address {}", seed.getAddress());
            this.hasSeed = true;
        }

        boolean hasUnknownPacket = false;
        while (buf.readableBytes() > 0 && !hasUnknownPacket) {
            var code = buf.getByte(buf.readerIndex());
            var hexCode = HexFormat.of().formatHex(new byte[] {code}).toUpperCase();

            var packetClass = packetsClass.get((int) code);
            if (packetClass != null) {
                final var startIndex = buf.readerIndex();
                // Consume the buffer
                out.add(packetClass.getConstructor(ByteBuf.class).newInstance(buf));

                if (log.isDebugEnabled()) {
                    final var length = buf.readerIndex() - startIndex;
                    var slice = buf.slice(startIndex, length);
                    var hexDump = ByteBufUtil.hexDump(slice).toUpperCase();
                    log.debug("Packet received [0x{} - {} - {}]", hexCode, hexDump, packetClass.getSimpleName());
                }

            } else {
                hasUnknownPacket = true;
                log.info(
                        "Unknown packet [0x{}] is not possible to decode remaining data.", hexCode);

                byte[] remainingData = new byte[buf.readableBytes()];
                buf.readBytes(remainingData);
                log.debug(
                        "Unknown packet data [{}]",
                        HexFormat.of().formatHex(remainingData).toUpperCase());
            }
        }
    }
}
