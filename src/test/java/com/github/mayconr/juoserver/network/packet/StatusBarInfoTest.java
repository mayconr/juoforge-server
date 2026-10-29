package com.github.mayconr.juoserver.network.packet;

import com.github.mayconr.juoserver.game.model.Gender;
import com.github.mayconr.juoserver.game.model.Race;
import com.github.mayconr.juoserver.game.model.UOMobile;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class StatusBarInfoTest {
    @ParameterizedTest
    @CsvSource({"80, 80", "0, 0", "-1, 0", "70000, 65535"})
    void sendsCalculatedMaxWeightAsUnsignedShort(int maxWeight, int expected) {
        var mobile = mock(UOMobile.class);
        when(mobile.getDisplayName()).thenReturn("Player");
        when(mobile.getRace()).thenReturn(Race.HUMAN);
        when(mobile.getGender()).thenReturn(Gender.MALE);
        when(mobile.getMaxWeight()).thenReturn(999);
        var buffer = Unpooled.buffer();
        try {
            new StatusBarInfo(mobile, 27, maxWeight).writesTo(buffer);
            assertEquals(27, buffer.getUnsignedShort(64));
            assertEquals(expected, buffer.getUnsignedShort(66));
        } finally {
            buffer.release();
        }
    }
}
