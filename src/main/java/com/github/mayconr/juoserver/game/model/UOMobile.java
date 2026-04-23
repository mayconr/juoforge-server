package com.github.mayconr.juoserver.game.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@Setter
@Slf4j
@ToString(onlyExplicitlyIncluded = true, callSuper = true)
/**
 * Represents a mobile entity in the game world (player or NPC).
 *
 * This class is the in-memory domain representation of a mobile and is backed by {@link UOMobileData}.
 * It contains combat stats, attributes, equipment, and state flags.
 */
public class UOMobile extends UOObject<UOMobileData> {

    /**
     * Maximum serial ID reserved for mobiles.
     */
    private static final int MOBILES_MAX_SERIAL_ID = 0x3FFFFFFF;

    // =========================
    // Identity & Core State
    // =========================

    /** Unique runtime identifier (not persisted). */
    private UUID id;

    /** Current facing direction. */
    private Direction direction;

    /** Visual hue (color). */
    private int hue;

    /** Current character status (e.g., war mode). */
    private CharacterStatus status;

    /** Reputation / notoriety (e.g., innocent, criminal). */
    private Notoriety notoriety;

    /** Whether the mobile is running. */
    private boolean running;

    /** Character race. */
    private Race race;

    /** Character gender. */
    private Gender gender;

    /** Mobile type (e.g., 'P' for player, 'N' for NPC). */
    private String type;

    /** Whether the mobile is alive. */
    private boolean alive;

    // =========================
    // Vitals (Dynamic Resources)
    // =========================

    private int hitpoints;
    private int maxHitpoints;

    private int stamina;
    private int maxStamina;

    private int mana;
    private int maxMana;

    // =========================
    // Primary Stats
    // =========================

    private int strength;
    private int dexterity;
    private int intelligence;

    /** Maximum total stat cap. */
    private int statCap;

    // =========================
    // Inventory & Capacity
    // =========================

    private int gold;

    /** Current carried weight. */
    private int weight;

    /** Maximum carrying capacity. */
    private int maxWeight;

    // =========================
    // Followers / Pets
    // =========================

    private int followers;
    private int maxFollowers;

    // =========================
    // Resistances
    // =========================

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

    // =========================
    // Combat & Bonuses
    // =========================

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

    // =========================
    // Equipment
    // =========================

    /** Backpack serial ID. */
    private Integer backpack;

    /** Equipped items mapped by layer. */
    private Map<Layer, Integer> equippedItems;

    // =========================
    // Skills
    // =========================

    private SkillContainer skills;

    // =========================
    // Constructor
    // =========================

    public UOMobile(UOMobileData data) {
        super(data);

        this.id = UUID.randomUUID();

        // Core
        this.direction = data.getDirection();
        this.hue = data.getHue();
        this.status = data.getStatus();
        this.notoriety = data.getNotoriety();
        this.running = data.isRunning();
        this.race = data.getRace();
        this.gender = data.getGender();
        this.type = data.getType();
        this.alive = data.isAlive();

        // Vitals
        this.hitpoints = data.getHitpoints();
        this.maxHitpoints = data.getMaxHitpoints();
        this.stamina = data.getStamina();
        this.maxStamina = data.getMaxStamina();
        this.mana = data.getMana();
        this.maxMana = data.getMaxMana();

        // Stats
        this.strength = data.getStrength();
        this.dexterity = data.getDexterity();
        this.intelligence = data.getIntelligence();
        this.statCap = data.getStatCap();

        // Inventory
        this.gold = data.getGold();
        this.weight = data.getWeight();
        this.maxWeight = data.getMaxWeight();

        // Followers
        this.followers = data.getFollowers();
        this.maxFollowers = data.getMaxFollowers();

        // Resistances
        this.physicalResist = data.getPhysicalResist();
        this.maxPhysicalResist = data.getMaxPhysicalResist();
        this.fireResist = data.getFireResist();
        this.maxFireResist = data.getMaxFireResist();
        this.coldResist = data.getColdResist();
        this.maxColdResist = data.getMaxColdResist();
        this.poisonResist = data.getPoisonResist();
        this.maxPoisonResist = data.getMaxPoisonResist();
        this.energyResist = data.getEnergyResist();
        this.maxEnergyResist = data.getMaxEnergyResist();

        // Combat
        this.luck = data.getLuck();
        this.damageMin = data.getDamageMin();
        this.damageMax = data.getDamageMax();
        this.tithingPoints = data.getTithingPoints();

        this.defenseChanceIncrease = data.getDefenseChanceIncrease();
        this.maxDefenseChanceIncrease = data.getMaxDefenseChanceIncrease();
        this.hitChanceIncrease = data.getHitChanceIncrease();
        this.swingSpeedIncrease = data.getSwingSpeedIncrease();
        this.weaponDamageIncrease = data.getWeaponDamageIncrease();
        this.lowerReagentCost = data.getLowerReagentCost();
        this.spellDamageIncrease = data.getSpellDamageIncrease();
        this.reflectPhysicalDamage = data.getReflectPhysicalDamage();
        this.enhancePotions = data.getEnhancePotions();
        this.fasterCastRecovery = data.getFasterCastRecovery();
        this.fasterCasting = data.getFasterCasting();
        this.lowerManaCost = data.getLowerManaCost();

        // Equipment
        this.equippedItems = new ConcurrentHashMap<>();
        if (data.getEquippedItems() != null) {
            data.getEquippedItems().forEach(this::equipItem);
        }

        // Skills
        this.skills = new SkillContainer(
                Optional.ofNullable(data.getSkills())
                        .map(Map::values)
                        .orElse(new ArrayList<>())
        );
    }

    // =========================
    // Persistence Mapping
    // =========================

    @Override
    protected UOMobileData createData() {
        return new UOMobileData();
    }

    @Override
    protected void populateData(UOMobileData data) {
        super.populateData(data);

        data.setDirection(direction);
        data.setHue(hue);
        data.setStatus(status);
        data.setNotoriety(notoriety);
        data.setRunning(running);
        data.setRace(race);
        data.setGender(gender);
        data.setType(type);
        data.setAlive(alive);

        data.setHitpoints(hitpoints);
        data.setMaxHitpoints(maxHitpoints);
        data.setStamina(stamina);
        data.setMaxStamina(maxStamina);
        data.setMana(mana);
        data.setMaxMana(maxMana);

        data.setStrength(strength);
        data.setDexterity(dexterity);
        data.setIntelligence(intelligence);
        data.setStatCap(statCap);

        data.setGold(gold);
        data.setWeight(weight);
        data.setMaxWeight(maxWeight);

        data.setFollowers(followers);
        data.setMaxFollowers(maxFollowers);

        data.setPhysicalResist(physicalResist);
        data.setMaxPhysicalResist(maxPhysicalResist);
        data.setFireResist(fireResist);
        data.setColdResist(coldResist);
        data.setPoisonResist(poisonResist);
        data.setMaxPoisonResist(maxPoisonResist);
        data.setEnergyResist(energyResist);

        data.setLuck(luck);
        data.setDamageMin(damageMin);
        data.setDamageMax(damageMax);
        data.setTithingPoints(tithingPoints);

        data.setDefenseChanceIncrease(defenseChanceIncrease);

        data.setSkills(skills.getSkillMap());
        data.setEquippedItems(equippedItems);
    }

    // =========================
    // Equipment Logic
    // =========================

    public void equipItem(UOItem item) {
        equipItem(item.getLayer(), item.getSerialId());
    }

    private void equipItem(Layer layer, Integer serialId) {
        if (Layer.BACKPACK.equals(layer)) {
            this.backpack = serialId;
        }
        equippedItems.put(layer, serialId);
    }

    public void unequipItem(UOItem item) {
        equippedItems.remove(item.getLayer());
    }

    public boolean isItemEquipped(UOItem item) {
        return equippedItems.containsValue(item.getSerialId());
    }

    public boolean isLayerAvailable(Layer layer) {
        return !equippedItems.containsKey(layer);
    }

    public void setBackpack(UOContainer backpack) {
        this.backpack = backpack.getSerialId();
        equippedItems.put(Layer.BACKPACK, backpack.getSerialId());
    }

    public boolean isMounted() {
        return equippedItems.containsKey(Layer.MOUNT);
    }

    // =========================
    // State Helpers
    // =========================

    public boolean isWarMode() {
        return CharacterStatus.WAR_MODE.equals(status);
    }

    public static boolean isMobile(int serialId) {
        return serialId <= MOBILES_MAX_SERIAL_ID;
    }

    /**
     * Registers the killer of this mobile for persistence/logging.
     */
    public void registerKiller(UOObject<?> killer) {
        persistentAttributes()
                .add(AttributeKeys.MOBILE_KILLED_BY, killer.getSerialId());
    }

    /**
     * @deprecated Movement should be handled by movement systems / game loop.
     */
    @Deprecated
    public void move(Direction direction) {
        this.direction = direction;
        setLocation(getX() + direction.getDx(), getY() + direction.getDy());
    }
}