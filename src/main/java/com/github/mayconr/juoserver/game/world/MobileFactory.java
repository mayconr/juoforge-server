package com.github.mayconr.juoserver.game.world;

import com.github.mayconr.juoserver.game.mobile.npc.template.NpcTemplate;
import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.game.player.PlayerDetails;

import java.util.Collections;
import java.util.UUID;

public class MobileFactory {

    public static UOPlayer createNewPlayer(SerialGenerator serialGenerator, PlayerDetails details) {
        final var player =  new UOPlayer(new UOMobile(
                UUID.randomUUID(),
                serialGenerator.getNextMobile(),
                details.bodyTemplate().modelId(),
                details.location().getX(),
                details.location().getY(),
                details.location().getZ(),
                details.name(),
                details.name(),
                Collections.emptyMap(),
                Direction.NORTH,
                details.skinColor(),
                CharacterStatus.NORMAL,
                Notoriety.INNOCENT,
                false,
                details.bodyTemplate().race(),
                details.bodyTemplate().gender(),
                details.status().strength(),
                details.status().strength(),
                details.status().strength(),
                details.status().dexterity(),
                details.status().intelligence(),
                details.status().dexterity(),
                details.status().dexterity(),
                details.status().intelligence(),
                details.status().intelligence(),
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
        player.setSkills(new SkillContainer(details.skills()));
        player.setType("P");
        return player;
    }

    public static UONpc createNpcFromTemplate(SerialGenerator serialGenerator, NpcTemplate template, Location location) {
        var npc = new UONpc(new UOMobile(
                UUID.randomUUID(),
                serialGenerator.getNextMobile(),
                template.modelId(),
                location.getX(),
                location.getY(),
                location.getZ(),
                template.name(),
                template.displayName(),
                template.attr(),
                Direction.NORTH,
                template.hue(),
                CharacterStatus.NORMAL,
                template.notoriety(),
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
                0
        ));
        npc.setBehavior(template.behavior());
        npc.setType("N");
        if (template.roles() != null) {
            for (String role : template.roles()) {
                npc.addRole(role);
            }
        }
        return npc;
    }
}
