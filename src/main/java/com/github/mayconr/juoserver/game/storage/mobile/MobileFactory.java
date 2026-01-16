package com.github.mayconr.juoserver.game.storage.mobile;

import com.github.mayconr.juoserver.game.core.model.*;

import java.util.UUID;

public class MobileFactory {

    public static UOPlayer createNewPlayer(PlayerDetails details) {
        return new UOPlayer(new UOMobile(
                UUID.randomUUID(),
                -1,
                0x190,
                2514,
                550,
                0,
                details.name(),
                Direction.NORTH,
                0x83EA,
                CharacterStatus.NORMAL,
                Notoriety.INNOCENT,
                false,
                Race.HUMAN,
                Gender.MALE,
                80,
                100,
                50,
                50,
                100,
                50,
                100,
                50,
                100,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0
        ), details.account().getId());
    }

}
