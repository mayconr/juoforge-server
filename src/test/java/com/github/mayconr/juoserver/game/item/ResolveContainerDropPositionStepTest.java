package com.github.mayconr.juoserver.game.item;

import com.github.mayconr.juoserver.game.item.flow.drop.DropItemContext;
import com.github.mayconr.juoserver.game.item.flow.drop.DropItemContext.TargetContainer;
import com.github.mayconr.juoserver.game.item.flow.drop.placement.ResolveContainerDropPositionStep;
import com.github.mayconr.juoserver.game.model.ItemFlag;
import com.github.mayconr.juoserver.game.model.ItemLocationType;
import com.github.mayconr.juoserver.game.model.PointInTheWorld;
import com.github.mayconr.juoserver.game.model.UOContainer;
import com.github.mayconr.juoserver.game.model.UOItemData;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.network.packet.DropItem;
import io.netty.buffer.Unpooled;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class ResolveContainerDropPositionStepTest {
    private static final int UNPOSITIONED = DropItem.NO_CONTAINER_POSITION;
    private final ResolveContainerDropPositionStep step = new ResolveContainerDropPositionStep();

    @Test
    void clientDropWithoutPositionIsReadAsTheNoPositionMarker() {
        var buffer = Unpooled.buffer();
        buffer.writeByte(0x08);
        buffer.writeInt(0x40000001);
        buffer.writeShort(0xFFFF);
        buffer.writeShort(0xFFFF);
        buffer.writeByte(0);
        buffer.writeByte(0);
        buffer.writeInt(0x00000001);
        try {
            var drop = new DropItem(buffer);

            assertEquals(UNPOSITIONED, drop.getX());
            assertEquals(UNPOSITIONED, drop.getY());
        } finally {
            buffer.release();
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 4, -1, 127})
    void dropWithoutPositionIsPlacedAtTheContainerOriginKeepingTheGridIndex(int gridIndex) {
        var context = context(new PointInTheWorld(UNPOSITIONED, UNPOSITIONED, gridIndex));

        assertTrue(step.execute(context).flowSucceeded());

        assertEquals(new PointInTheWorld(0, 0, gridIndex), context.getTargetContainer().location());
    }

    @Test
    void resolvedPositionSatisfiesThePersistedPositionRules() {
        var context = context(new PointInTheWorld(UNPOSITIONED, UNPOSITIONED, 0));

        step.execute(context);

        var location = context.getTargetContainer().location();
        assertTrue(location.getX() >= 0 && location.getY() >= 0, "violates chk_item_position: " + location);
    }

    @Test
    void keepsTheTargetContainer() {
        var container = container();
        var context = DropItemContext.ofContainer(
                mock(UOPlayer.class),
                new TargetContainer(container, new PointInTheWorld(UNPOSITIONED, UNPOSITIONED, 0))
        );

        step.execute(context);

        assertSame(container, context.getTargetContainer().container());
    }

    @ParameterizedTest
    @CsvSource({"80, 90", "0, 0", "-1, 0", "0, -1", "300, 400"})
    void dropWithAPositionIsKeptAsRequested(int x, int y) {
        var target = new TargetContainer(container(), new PointInTheWorld(x, y, 2));
        var context = DropItemContext.ofContainer(mock(UOPlayer.class), target);

        step.execute(context);

        assertSame(target, context.getTargetContainer());
    }

    private static DropItemContext context(PointInTheWorld location) {
        return DropItemContext.ofContainer(mock(UOPlayer.class), new TargetContainer(container(), location));
    }

    private static UOContainer container() {
        var data = new UOItemData();
        data.setSerialId(0x40000010);
        data.setFlags(List.of(ItemFlag.CONTAINER));
        data.setLocationType(ItemLocationType.ORPHAN);
        return new UOContainer(data, null);
    }
}
