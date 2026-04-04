package com.github.mayconr.juoserver.game.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true, callSuper = true)
public class UOMobile extends UOObject<UOMobileData> implements Container {

    private static final int MOBILES_MAX_SERIAL_ID = 0x3FFFFFFF;

    private UUID id;
    private Direction direction;
    private int hue;
    private CharacterStatus status; // TODO statusmap
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

    // Items
    private UOContainer backpack;
    private Map<Layer, UOItem> equippedItems = new ConcurrentHashMap<>();

    // Skills
    private SkillContainer skills;

    public UOMobile(int serialId, int modelId, int x, int y, int z, String name, String displayName, AttributeMap persistentAttrMap) {
        super(serialId, modelId, x, y, z, name, displayName, persistentAttrMap);
        this.alive = true;
    }

    public UOMobile(UOMobile other) {
        super(
                other.getSerialId(),
                other.getModelId(),
                other.getX(),
                other.getY(),
                other.getZ(),
                other.getName(),
                other.getDisplayName(),
                other.persistentAttributes());
        this.id = other.id;
        this.direction = other.getDirection();
        this.hue = other.getHue();
        this.status = other.getStatus();
        this.notoriety = other.getNotoriety();
        this.running = other.isRunning();
        this.race = other.getRace();
        this.gender = other.getGender();
        this.type = other.getType();
        this.alive = other.isAlive();

        this.hitpoints = other.getHitpoints();
        this.maxHitpoints = other.getMaxHitpoints();

        this.strength = other.getStrength();
        this.dexterity = other.getDexterity();
        this.intelligence = other.getIntelligence();

        this.stamina = other.getStamina();
        this.maxStamina = other.getMaxStamina();

        this.mana = other.getMana();
        this.maxMana = other.getMaxMana();

        this.gold = other.getGold();
        this.weight = other.getWeight();
        this.maxWeight = other.getMaxWeight();
        this.statCap = other.getStatCap();

        this.followers = other.getFollowers();
        this.maxFollowers = other.getMaxFollowers();

        this.physicalResist = other.getPhysicalResist();
        this.maxPhysicalResist = other.getMaxPhysicalResist();
        this.fireResist = other.getFireResist();
        this.maxFireResist = other.getMaxFireResist();
        this.coldResist = other.getColdResist();
        this.maxColdResist = other.getMaxColdResist();
        this.poisonResist = other.getPoisonResist();
        this.maxPoisonResist = other.getMaxPoisonResist();
        this.energyResist = other.getEnergyResist();
        this.maxEnergyResist = other.getMaxEnergyResist();

        this.luck = other.getLuck();
        this.damageMin = other.getDamageMin();
        this.damageMax = other.getDamageMax();

        this.tithingPoints = other.getTithingPoints();

        this.defenseChanceIncrease = other.getDefenseChanceIncrease();
        this.maxDefenseChanceIncrease = other.getMaxDefenseChanceIncrease();
        this.hitChanceIncrease = other.getHitChanceIncrease();
        this.swingSpeedIncrease = other.getSwingSpeedIncrease();
        this.weaponDamageIncrease = other.getWeaponDamageIncrease();
        this.lowerReagentCost = other.getLowerReagentCost();
        this.spellDamageIncrease = other.getSpellDamageIncrease();
        this.reflectPhysicalDamage = other.getReflectPhysicalDamage();
        this.enhancePotions = other.getEnhancePotions();
        this.fasterCastRecovery = other.getFasterCastRecovery();
        this.fasterCasting = other.getFasterCasting();
        this.lowerManaCost = other.getLowerManaCost();

        this.backpack = other.getBackpack();
        this.equippedItems = other.getEquippedItems();

        this.skills = other.skills;
    }

    public UOMobile(
            UUID id,
            int serialId,
            int modelId,
            int x,
            int y,
            int z,
            String name,
            String displayName,
            AttributeMap attrMap,
            Direction direction,
            int hue,
            CharacterStatus status,
            Notoriety notoriety,
            boolean running,
            Race race,
            Gender gender,
            int hitpoints,
            int maxHitpoints,
            int strength,
            int dexterity,
            int intelligence,
            int stamina,
            int maxStamina,
            int mana,
            int maxMana,
            int gold,
            int weight,
            int maxWeight,
            int statCap,
            int followers,
            int maxFollowers,
            int physicalResist,
            int maxPhysicalResist,
            int fireResist,
            int maxFireResist,
            int coldResist,
            int maxColdResist,
            int poisonResist,
            int maxPoisonResist,
            int energyResist,
            int maxEnergyResist,
            int luck,
            int damageMin,
            int damageMax,
            int tithingPoints,
            int defenseChanceIncrease,
            int maxDefenseChanceIncrease,
            int hitChanceIncrease,
            int swingSpeedIncrease,
            int weaponDamageIncrease,
            int lowerReagentCost,
            int spellDamageIncrease,
            int reflectPhysicalDamage,
            int enhancePotions,
            int fasterCastRecovery,
            int fasterCasting,
            int lowerManaCost) {
        super(serialId, modelId, x, y, z, name, displayName, attrMap);
        this.id = id;
        this.direction = direction;
        this.hue = hue;
        this.status = status;
        this.notoriety = notoriety;
        this.running = running;
        this.race = race;
        this.gender = gender;
        this.alive = true;

        this.hitpoints = hitpoints;
        this.maxHitpoints = maxHitpoints;

        this.strength = strength;
        this.dexterity = dexterity;
        this.intelligence = intelligence;

        this.stamina = stamina;
        this.maxStamina = maxStamina;

        this.mana = mana;
        this.maxMana = maxMana;

        this.gold = gold;
        this.weight = weight;
        this.maxWeight = maxWeight;
        this.statCap = statCap;

        this.followers = followers;
        this.maxFollowers = maxFollowers;

        this.physicalResist = physicalResist;
        this.maxPhysicalResist = maxPhysicalResist;
        this.fireResist = fireResist;
        this.maxFireResist = maxFireResist;
        this.coldResist = coldResist;
        this.maxColdResist = maxColdResist;
        this.poisonResist = poisonResist;
        this.maxPoisonResist = maxPoisonResist;
        this.energyResist = energyResist;
        this.maxEnergyResist = maxEnergyResist;

        this.luck = luck;
        this.damageMin = damageMin;
        this.damageMax = damageMax;

        this.tithingPoints = tithingPoints;

        this.defenseChanceIncrease = defenseChanceIncrease;
        this.maxDefenseChanceIncrease = maxDefenseChanceIncrease;
        this.hitChanceIncrease = hitChanceIncrease;
        this.swingSpeedIncrease = swingSpeedIncrease;
        this.weaponDamageIncrease = weaponDamageIncrease;
        this.lowerReagentCost = lowerReagentCost;
        this.spellDamageIncrease = spellDamageIncrease;
        this.reflectPhysicalDamage = reflectPhysicalDamage;
        this.enhancePotions = enhancePotions;
        this.fasterCastRecovery = fasterCastRecovery;
        this.fasterCasting = fasterCasting;
        this.lowerManaCost = lowerManaCost;
        this.skills = new SkillContainer();
    }

    @Override
    protected UOMobileData createData() {
        return null;
    }

    public void equipItem(UOItem item) {
        if (Layer.BACKPACK.equals(item.getLayer())) {
            this.backpack = (UOContainer) item;
        }
        equipItem(item.getLayer(), item);
    }

    public void equipItem(Layer layer, UOItem item) {
        equippedItems.put(layer, item);
        item.equip(this);
    }

    public void unequipItem(UOItem item) {
        item.unequip();
        equippedItems.remove(item.getLayer());
    }

    public boolean isItemEquipped(UOItem item) {
        return equippedItems.containsValue(item);
    }

    public boolean isLayerAvailable(Layer layer) {
        // TODO when one hand or two hand, must check both layers
        return !equippedItems.containsKey(layer);
    }

    public void setBackpack(UOContainer backpack) {
        this.backpack = backpack;
        equippedItems.put(Layer.BACKPACK, backpack);
    }

    @Override
    public void addItemsToContainer(List<UOItem> items) {
        for (UOItem item : items) {
            addItemToContainer(item);
        }
    }

    @Override
    public void addItemToContainer(UOItem item) {
        if (backpack == null) {
            throw new IllegalStateException("Backpack does not exist for player " + getName());
        }
        backpack.addItemToContainer(item);
    }

    @Override
    public void addItemToContainer(UOItem item, Location locationInContainer) {
        if (backpack == null) {
            throw new IllegalStateException("Backpack does not exist for player " + getName());
        }
        backpack.addItemToContainer(item, locationInContainer);
    }

    @Override
    public void removeItemFromContainer(UOItem item) {
        backpack.removeItemFromContainer(item);
    }

    @Override
    public Collection<UOItem> getContainerItems() {
        return backpack.getContainerItems();
    }

    @Override
    public int getContainerGumpId() {
        return backpack.getContainerGumpId();
    }

    @Deprecated
    public void move(Direction direction) {
        this.direction = direction;
        setLocation(getX() + direction.getDx(), getY() + direction.getDy());
    }

    public boolean isMounted() {
        return equippedItems.containsKey(Layer.MOUNT);
    }

    public boolean isWarMode() {
        return CharacterStatus.WAR_MODE.equals(status);
    }

    public static boolean isMobile(int serialId) {
        return serialId <= MOBILES_MAX_SERIAL_ID;
    }
}
