package com.github.mayconr.juoserver.game.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
public class UOMobileData extends UOObjectData {

    private int ghostModelId;
    private Direction direction;
    private int hue;
    private CharacterStatus status;
    private Notoriety notoriety;
    private boolean running;
    private Race race;
    private Gender gender;
    private String type;
    private boolean alive;

    // vitals
    private int hitpoints;
    private int maxHitpoints;

    private int strength;
    private int dexterity;
    private int intelligence;

    private int stamina;
    private int maxStamina;

    private int mana;
    private int maxMana;

    private int gold;
    private int weight;
    private int maxWeight;
    private int statCap;

    private int followers;
    private int maxFollowers;

    private int physicalResist;
    private int maxPhysicalResist;

    private int fireResist;
    private int maxFireResist;

    private int coldResist;
    private int maxColdResist;

    private int poisonResist;
    private int maxPoisonResist;

    private int energyResist;
    private int maxEnergyResist;

    private int luck;

    private int damageMin;
    private int damageMax;

    private int tithingPoints;

    private int defenseChanceIncrease;
    private int maxDefenseChanceIncrease;
    private int hitChanceIncrease;
    private int swingSpeedIncrease;
    private int weaponDamageIncrease;
    private int lowerReagentCost;
    private int spellDamageIncrease;
    private int reflectPhysicalDamage;
    private int enhancePotions;
    private int fasterCastRecovery;
    private int fasterCasting;
    private int lowerManaCost;

    private Map<Layer, Integer> equippedItems;
    private Map<Integer, SkillValue> skills;

    // NPC
    private int speechHue;
    private int speechFont;
    private BehaviorDefinition behavior;
    private List<String> roles;

    // Player
    private UUID accountId;
}
