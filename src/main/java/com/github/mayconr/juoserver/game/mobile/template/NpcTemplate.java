package com.github.mayconr.juoserver.game.mobile.template;

import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.infrastructure.template.BaseTemplate;

import java.util.List;
import java.util.Map;

public record NpcTemplate(String name,
                          String displayName,
                          int modelId,
                          Notoriety notoriety,
                          int hue,
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
        maxHitpoints = maxHitpoints == null ? 100 : maxHitpoints;
        if (maxHitpoints <= 0) {
            throw new IllegalArgumentException("NPC maxHitpoints must be positive: " + name);
        }
        maxStamina = maxStamina == null ? 100 : maxStamina;
        if (maxStamina <= 0) {
            throw new IllegalArgumentException("NPC maxStamina must be positive: " + name);
        }
        maxMana = maxMana == null ? 100 : maxMana;
        if (maxMana <= 0) {
            throw new IllegalArgumentException("NPC maxMana must be positive: " + name);
        }
        strength = strength == null ? 100 : strength;
        if (strength <= 0) {
            throw new IllegalArgumentException("NPC strength must be positive: " + name);
        }
        intelligence = intelligence == null ? 100 : intelligence;
        if (intelligence <= 0) {
            throw new IllegalArgumentException("NPC intelligence must be positive: " + name);
        }
        dexterity = dexterity == null ? 100 : dexterity;
        if (dexterity <= 0) {
            throw new IllegalArgumentException("NPC dexterity must be positive: " + name);
        }
        attr = attr == null ? Map.of() : attr;
        equippedItems = equippedItems == null ? List.of() : equippedItems;
        roles = roles == null ? List.of() : roles;
    }

    public UOMobileData toData(int serialId, Map<Layer, Integer> equippedItems, Location location) {
        UOMobileData data = new UOMobileData();
        data.setSerialId(serialId);
        data.setName(name);
        data.setDisplayName(displayName);
        data.setModelId(modelId);
        data.setHue(hue);
        data.setMaxHitpoints(maxHitpoints);
        data.setHitpoints(maxHitpoints);
        data.setMaxStamina(maxStamina);
        data.setStamina(maxStamina);
        data.setMaxMana(maxMana);
        data.setMana(maxMana);
        data.setStrength(strength);
        data.setDexterity(dexterity);
        data.setIntelligence(intelligence);
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
