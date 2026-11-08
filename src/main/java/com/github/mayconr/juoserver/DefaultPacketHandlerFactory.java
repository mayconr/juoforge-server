package com.github.mayconr.juoserver;

import com.github.mayconr.juoserver.game.world.WorldInternal;
import com.github.mayconr.juoserver.network.handler.*;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.List;

import static java.util.List.*;

public class DefaultPacketHandlerFactory {
    public List<SimpleChannelInboundHandler<?>> create(
            WorldInternal world
    ) {
        return of(
                new GameServerLoginHandler(),
                new PingPongHandler(),
                new LoginCharacterHandler(),
                new DeleteCharacterHandler(),
                new CreatePlayerHandler(),
                new ClientVersionHandler(),
                new OpenChatRequestHandler(),
                new MoveRequestHandler(world),
                new DoubleClickHandler(world),
                new UnicodeSpeachRequestHandler(world),
                new TooltipRequestHandler(world),
                new GeneralInformationHandler(world),
                new SingleClickHandler(world),
                new ItemUnequippedHandler(world),
                new DropItemHandler(world),
                new EquipItemHandler(world),
                new TargetHandler(world),
                new GetPlayerStatusHandler(world),
                new RequestHelpHandler(world),
                new RequestWarModeHandler(world),
                new AttackRequestHandler(world),
                new GumpSelectionHandler(world),
                new UseRequestHandler(world),
                new ActionRequestedHandler(world),
                new SendSkillHandler(world),
                new VendorBuyRequestHandler(world),
                new ResyncRequestHandler()
        );
    }
}
