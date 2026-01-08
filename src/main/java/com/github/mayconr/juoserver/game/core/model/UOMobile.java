package com.github.mayconr.juoserver.game.core.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.github.mayconr.juoserver.game.core.prototype.NpcPrototype;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true, callSuper = true)
public class UOMobile extends UOObject implements Container {
    private Direction direction;
    private int hue;
    private CharacterStatus status;
    private Notoriety notoriety;
    private boolean running;
    private Race race;
    private Gender gender;

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
    private int hpRegen;
    private int staminaRegen;
    private int manaRegen;
    private int reflectPhysicalDamage;
    private int enhancePotions;
    private int fasterCastRecovery;
    private int fasterCasting;
    private int lowerManaCost;

    // Items
    private UOContainer backpack;
    private Map<Layer, UOItem> equippedItems = new HashMap<>();

    public UOMobile(
            int serialId,
            int modelId,
            int x,
            int y,
            int z,
            String name,
            Direction direction,
            int hue,
            CharacterStatus status,
            Notoriety notoriety,
            Race race,
            Gender gender) {
        super(serialId, modelId, x, y, z, name);
        this.direction = direction;
        this.hue = hue;
        this.status = status;
        this.notoriety = notoriety;
        this.race = race;
        this.gender = gender;
    }

    public UOMobile(
            int serialId,
            Location location,
            Direction direction,
            CharacterStatus status,
            NpcPrototype prototype) {
        super(serialId, prototype.getModelId(), location, prototype.getName());
        this.direction = direction;
        this.hue = prototype.getHue();
        this.status = status;
        this.notoriety = prototype.getNotoriety();
        this.race = prototype.getRace();
        this.gender = prototype.getGender();
        this.maxHitpoints = prototype.getMaxHitpoints();
        this.maxStamina = prototype.getMaxStamina();
        this.maxMana = prototype.getMaxMana();
    }

    public void equipItem(Layer layer, UOItem item) {
        equippedItems.put(layer, item);
    }

    public boolean isItemEquipped(UOItem item) {
        return equippedItems.containsValue(item);
    }

    public void unequipItem(UOItem unequippedItem) {
        Layer layer = null;
        for (Map.Entry<Layer, UOItem> entry : equippedItems.entrySet()) {
            if (Objects.equals(unequippedItem, entry.getValue())) {
                layer = entry.getKey();
            }
        }
        if (layer != null) {
            equippedItems.remove(layer);
        }
    }

    public void setBackpack(UOContainer backpack) {
        this.backpack = backpack;
        equippedItems.put(Layer.BACKPACK, backpack);
    }

    @Override
    public void addItemToContainer(UOItem item) {
        if (backpack == null) {
            throw new IllegalStateException("Backpack does not exist for mobile " + getName());
        }
        item.setContainer(backpack);
        backpack.addItemToContainer(item);
    }

    @Override
    public void removeItemFromContainer(UOItem item) {
        item.setContainer(null);
        backpack.removeItemFromContainer(item);
    }

    @Override
    public Collection<UOItem> getItemsInContainer() {
        return backpack.getItemsInContainer();
    }

    @Override
    public int getContainerGumpId() {
        return backpack.getContainerGumpId();
    }

    public void move(Direction direction) {
        this.direction = direction;
        setLocation(getX() + direction.getDx(), getY() + direction.getDy());
    }

    public boolean isWarMode() {
        return CharacterStatus.WAR_MODE.equals(status);
    }
}
