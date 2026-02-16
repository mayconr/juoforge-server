package com.github.mayconr.juoserver.game.world;

import com.github.mayconr.juoserver.game.world.module.economy.VendorNpcRole;
import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.game.template.definitions.npc.NpcTemplate;

import java.util.Collections;
import java.util.UUID;

public class MobileFactory {

    public static UOPlayer createNewPlayer(SerialGenerator serialGenerator, PlayerDetails details) {
        final var player =  new UOPlayer(new UOMobile(
                UUID.randomUUID(),
                serialGenerator.nextMobileMobile(),
                0x190,
                details.location().getX(),
                details.location().getY(),
                details.location().getZ(),
                details.name(),
                details.name(),
                Collections.emptyMap(),
                Direction.NORTH,
                0x83EA,
                CharacterStatus.NORMAL,
                Notoriety.INNOCENT,
                false,
                Race.HUMAN,
                Gender.HUMAN_MALE,
                80,
                100,
                details.status().strength(),
                details.status().dexterity(),
                details.status().intelligence(),
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
        ), details.account().getId());
        for (UOItem item : details.equippedItems()) {
            player.equipItem(item);
        }
        player.setSkills(new SkillContainer(details.skills()));
        return player;
    }

    public static UONpc createNpcFromTemplate(SerialGenerator serialGenerator, NpcTemplate template, Location location) {
        var npc = new UONpc(new UOMobile(
                UUID.randomUUID(),
                serialGenerator.nextMobileMobile(),
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
                Gender.HUMAN_MALE,
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
        ), template.type());
        npc.setBehavior(template.behavior());
        if (template.roles() != null) {
            for (String role : template.roles()) {
                if ("vendor".equals(role)) {
                    npc.addRole(new VendorNpcRole("city-britain")); // TODO region must be resolved
                }
            }
        }
        return npc;
    }
}
