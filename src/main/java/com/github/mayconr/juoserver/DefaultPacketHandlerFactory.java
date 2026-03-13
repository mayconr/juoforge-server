package com.github.mayconr.juoserver;

import com.github.mayconr.juoserver.game.world.WorldInternal;
import com.github.mayconr.juoserver.network.handler.*;

public class DefaultPacketHandlerFactory {
    public java.util.List<io.netty.channel.SimpleChannelInboundHandler<?>> create(
            JuoforgeConfiguration configuration,
            WorldInternal world
    ) {
        return java.util.List.of(
                new GameServerLoginHandler( configuration.world().mobileStorage(), configuration.world().accountStorage()),
                new PingPongHandler(),
                new LoginCharacterHandler(),
                new DeleteCharacterHandler(),
                new CreatePlayerHandler(),
                new ClientVersionHandler(),
                new MoveRequestHandler(world),
                new DoubleClickHandler(world),
                new UnicodeSpeachRequestHandler(world),
                new TooltipRequestHandler(world),
                new GeneralInformationHandler(),
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
                new VendorBuyRequestHandler(world)
        );
    }
}
