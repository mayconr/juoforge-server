package com.github.mayconr.juoserver.game.mobile.template;

import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.infrastructure.template.BaseTemplate;

import com.github.mayconr.juoserver.game.npc.stats.NpcStats;

import java.util.List;
import java.util.Map;

public record NpcTemplate(String name,
                          String displayName,
                          int modelId,
                          Notoriety notoriety,
                          int hue,
                          String statProfile,
                          Integer maxHitpoints,
                          Integer maxStamina,
                          Integer maxMana,
                          Integer strength,
                          Integer dexterity,
                          Integer intelligence,
                          BehaviorDefinition behavior,
                          Race race,
                          Gender gender,
                          Map<String, Object> attr,
                          List<String> equippedItems,
                          List<String> roles)
        implements BaseTemplate {

    public NpcTemplate {
        attr = attr == null ? Map.of() : attr;
        equippedItems = equippedItems == null ? List.of() : equippedItems;
        roles = roles == null ? List.of() : roles;
    }

    public UOMobileData toData(int serialId, Map<Layer, Integer> equippedItems, Location location, NpcStats stats) {
        UOMobileData data = new UOMobileData();
        data.setSerialId(serialId);
        data.setName(name);
        data.setDisplayName(displayName);
        data.setModelId(modelId);
        data.setHue(hue);
        data.setMaxHitpoints(stats.maxHitpoints());
        data.setHitpoints(stats.maxHitpoints());
        data.setMaxStamina(stats.maxStamina());
        data.setStamina(stats.maxStamina());
        data.setMaxMana(stats.maxMana());
        data.setMana(stats.maxMana());
        data.setStrength(stats.strength());
        data.setDexterity(stats.dexterity());
        data.setIntelligence(stats.intelligence());
        data.setNotoriety(notoriety);
        data.setPersistentAttrMap(new DefaultAttributeMap(attr));
        data.setDirection(Direction.NORTH);
        data.setEquippedItems(equippedItems);
        data.setBehavior(behavior);
        data.setRoles(roles);
        data.setX(location.getX());
        data.setY(location.getY());
        data.setZ(location.getZ());
        //  Defaults
        data.setType("N");
        data.setAlive(true);
        data.setRunning(false);
        data.setStatus(CharacterStatus.NORMAL);
        data.setGender(gender == null ? Gender.MALE : gender);
        data.setRace(race == null ? Race.UNKNOWN : race);

        return data;
    }

}
