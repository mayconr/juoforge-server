package com.github.mayconr.shard.command;

import com.github.mayconr.juoserver.ServerRuntime;
import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.game.model.event.Prompt;
import com.github.mayconr.juoserver.game.world.World;
import com.github.mayconr.juoserver.game.world.ModuleOperationException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.function.Consumer;

import static org.mockito.Mockito.*;

class ModuleCommandFailureTest {
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test
    void createItemDisplaysLocalizedFailureInsideTargetCallback() {
        var world = mock(World.class, RETURNS_DEEP_STUBS);
        var player = mock(UOPlayer.class);
        var command = new CreateItem(world);
        var error = new ModuleOperationException("createItem", "ITEM_TEMPLATE_NOT_FOUND", "Missing item",
                "{item.template_not_found}", null);
        when(world.item().createItem(any(), any())).thenThrow(error);
        command.handle(new Prompt(player, "createitem", new String[]{"missing"}));
        ArgumentCaptor<Consumer<TargetResult>> callback = ArgumentCaptor.forClass((Class) Consumer.class);
        verify(world.interaction()).sendTarget(eq(player), eq(CursorType.NEUTRAL), callback.capture());

        callback.getValue().accept(new MobileTargetResult(player, player, player));

        verify(world.message()).send(player, "{item.template_not_found}");
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test
    void mountDisplaysLocalizedFailureInsideTargetCallback() {
        var world = mock(World.class, RETURNS_DEEP_STUBS);
        var runtime = mock(ServerRuntime.class);
        when(runtime.world()).thenReturn(world);
        var player = mock(UOPlayer.class);
        var npc = mock(UONpc.class);
        var mobiles = world.mobile();
        doThrow(new ModuleOperationException("mount", "MOUNT_OUT_OF_RANGE", "Outside range",
                "{mount.out_of_range}", null)).when(mobiles).mount(player, npc);
        new Mount(runtime).handle(new Prompt(player, "mount", new String[0]));
        ArgumentCaptor<Consumer<TargetResult>> callback = ArgumentCaptor.forClass((Class) Consumer.class);
        verify(world.interaction()).sendTarget(eq(player), eq(CursorType.NEUTRAL), callback.capture());

        callback.getValue().accept(new MobileTargetResult(player, npc, npc));

        verify(world.message()).send(player, "{mount.out_of_range}");
    }
}
