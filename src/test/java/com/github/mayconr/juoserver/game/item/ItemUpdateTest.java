package com.github.mayconr.juoserver.game.item;

import com.github.mayconr.juoserver.game.item.flow.update.ApplyItemChangeStep;
import com.github.mayconr.juoserver.game.item.flow.update.UpdateItemContext;
import com.github.mayconr.juoserver.game.model.ItemLocationType;
import com.github.mayconr.juoserver.game.model.PointInTheWorld;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.UOItemData;
import com.github.mayconr.juoserver.game.model.event.ItemUpdated;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.storage.WorldStorage;
import com.github.mayconr.juoserver.infrastructure.storage.item.Items;
import com.github.mayconr.juoserver.network.packet.DeleteObject;
import com.github.mayconr.juoserver.network.packet.ObjectInfo;
import com.github.mayconr.juoserver.network.session.SessionPacketWriter;
import com.github.mayconr.juoserver.network.session.SessionView;
import com.github.mayconr.juoserver.network.session.handler.ItemEventHandler;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ItemUpdateTest {
    private final WorldStorage storage = mock(WorldStorage.class);
    private final Items index = mock(Items.class);
    private final EventBus events = mock(EventBus.class);
    private final ApplyItemChangeStep step = new ApplyItemChangeStep(storage, events);

    @Test
    void movesIndexFromOldPositionAndPublishesCompleteChange() {
        var item = item(ItemLocationType.GROUND);
        when(storage.items()).thenReturn(index);
        doAnswer(call -> {
            assertEquals(0, item.getX());
            return null;
        }).when(index).removeFromGround(item);
        doAnswer(call -> {
            assertEquals(100, item.getX());
            return null;
        }).when(index).placeOnGround(item);

        step.execute(new UpdateItemContext(item, new ItemChange(new PointInTheWorld(100, 200, 5), 42)));

        assertEquals(42, item.toData().getModelId());
        assertEquals(100, item.toData().getX());
        verify(index).removeFromGround(item);
        verify(index).placeOnGround(item);
        var event = ArgumentCaptor.forClass(ItemUpdated.class);
        verify(events).publish(event.capture());
        assertEquals(0, event.getValue().previousLocation().getX());
        assertEquals(42, event.getValue().item().getModelId());
    }

    @Test
    void optionalFieldsPreserveExistingValuesAndEmptyChangeDoesNothing() {
        var item = item(ItemLocationType.GROUND);
        assertFalse(step.execute(new UpdateItemContext(item, new ItemChange())).flowFailed());
        verifyNoInteractions(storage, events);
        step.execute(new UpdateItemContext(item, new ItemChange(null, 42)));
        assertEquals(0, item.getX());
        assertEquals(42, item.getModelId());
        verifyNoInteractions(storage);
    }

    @Test
    void rejectsChangesOutsideGroundWithoutPartialMutation() {
        var item = item(ItemLocationType.CONTAINER);
        var change = new ItemChange(new PointInTheWorld(100, 0, 0), 42);
        assertTrue(step.execute(new UpdateItemContext(item, change)).flowFailed());
        assertEquals(0, item.getX());
        assertEquals(1, item.getModelId());
        verifyNoInteractions(storage, events);
    }

    @Test
    void removesItemForOldObserversAndDrawsForNewObservers() {
        var item = item(ItemLocationType.GROUND);
        var previous = new PointInTheWorld(100, 0, 0);
        var event = new ItemUpdated(item, null, previous);
        var session = mock(SessionView.class);
        var writer = mock(SessionPacketWriter.class);
        var handler = new ItemEventHandler(session, writer, null);
        when(session.shouldReceiveUpdate(previous)).thenReturn(true);
        handler.onItemUpdated(event);
        verify(writer).writeAndFlush(isA(DeleteObject.class));

        var newWriter = mock(SessionPacketWriter.class);
        when(session.shouldReceiveUpdate(item)).thenReturn(true);
        new ItemEventHandler(session, newWriter, null).onItemUpdated(event);
        verify(newWriter).writeAndFlush(isA(ObjectInfo.class));
    }

    private UOItem item(ItemLocationType locationType) {
        var data = new UOItemData();
        data.setLocationType(locationType);
        data.setModelId(1);
        data.setFlags(List.of());
        data.setContainerSerialId(100);
        return new UOItem(data, null);
    }
}
