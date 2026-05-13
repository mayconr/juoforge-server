package com.github.mayconr.juoserver.game.mobile.npc.template;

import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.infrastructure.template.BaseTemplate;

import java.util.List;
import java.util.Map;

public record NpcTemplate(String name,
                          String displayName,
                          int modelId,
                          Notoriety notoriety,
                          int hue,
                          BehaviorDefinition behavior,
                          Race race,
                          Gender gender,
                          Map<String, Object> attr,
                          List<String> equippedItems,
                          List<String> roles)
        implements BaseTemplate {

    public UOMobileData toData(int serialId, Map<Layer, Integer> equippedItems, Location location) {
        UOMobileData data = new UOMobileData();
        data.setSerialId(serialId);
        data.setName(name);
        data.setDisplayName(displayName);
        data.setModelId(modelId);
        data.setHue(hue);
        data.setNotoriety(notoriety);
        data.setPersistentAttrMap(new DefaultAttributeMap(attr));
        data.setDirection(Direction.NORTH);
        data.setEquippedItems(equippedItems);
        data.setBehavior(behavior);
        data.setRoles(roles);
        data.setX(location.getX());
        data.setY(location.getY());
        data.setZ(location.getZ());
        //  Defautls
        data.setType("N");
        data.setAlive(true);
        data.setRunning(false);
        data.setStatus(CharacterStatus.NORMAL);
        data.setGender(gender == null ? Gender.MALE : gender);
        data.setRace(race == null ? Race.UNKNOWN : race);

        return data;
    }

}
