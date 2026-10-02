package com.github.mayconr.shard.command;

import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.game.model.event.Prompt;
import com.github.mayconr.juoserver.game.world.World;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Info extends AbstractCommand{

    private final World world;

    public Info(World world) {
        super("info");
        this.world = world;
    }

    @Override
    public void handle(Prompt event) {
        world.interaction().sendTarget(event.player(), CursorType.NEUTRAL, res -> {
            if (res instanceof TileTargetResult statics) {
                logStatics(statics);
            } else if (res instanceof MobileTargetResult mobile) {
                logMobile(mobile.mobile());
            } else if (res instanceof ItemTargetResult item) {
                logItem(item.item());
            }
        });
    }

    private void logStatics(TileTargetResult statics) {
        var tiles = statics.staticsTile();
        var location = statics.location();
        var details = new StringBuilder("\n========== STATICS INFO ==========\n");
        details.append("Target position: (%d, %d, %d)%n".formatted(
                location.getX(), location.getY(), location.getZ()));
        details.append("Static tiles: ").append(tiles.size()).append('\n');
        if (tiles.isEmpty()) {
            details.append("No static tiles at this location.\n");
        }
        for (int i = 0; i < tiles.size(); i++) {
            var tile = tiles.get(i);
            details.append("""
                    >>> STATIC TILE %d <<<
                      Class: %s
                    [Identity]
                      ID: %d (0x%04X), Name: %s
                    [Location and dimensions]
                      Position: (%d, %d, %d), Height: %d
                    [Flags]
                      %s
                    """.formatted(i + 1, tile.getClass().getName(),
                    tile.id(), tile.id(), tile.name(),
                    tile.x(), tile.y(), tile.z(), tile.height(), tile.flags()));
        }
        details.append("==================================");
        log.info(details.toString());
    }

    private void logMobile(UOMobile target) {
        var details = new StringBuilder("\n========== MOBILE INFO ==========\n");
        details.append("Class: ").append(target.getClass().getName()).append('\n');
        if (target.getClass() != UOMobile.class) {
            details.append(">>> SUBCLASS: ").append(target.getClass().getSimpleName()).append(" <<<\n");
        }
        details.append("""
                [Identity]
                  Serial: %d (0x%08X), UUID: %s
                  Name: %s, Display name: %s, Type: %s
                  Model: %d, Hue: %d, Race: %s, Gender: %s
                [Location and state]
                  Position: (%d, %d, %d), Direction: %s
                  Alive: %s, Running: %s, Mounted: %s, War mode: %s
                  Status: %s, Notoriety: %s
                [Vitals and stats]
                  Hitpoints: %d/%d, Stamina: %d/%d, Mana: %d/%d
                  Strength: %d, Dexterity: %d, Intelligence: %d, Stat cap: %d
                  Gold: %d, Weight: %d/%d, Followers: %d/%d
                [Resistances (current/max)]
                  Physical: %d/%d, Fire: %d/%d, Cold: %d/%d
                  Poison: %d/%d, Energy: %d/%d
                [Combat and bonuses]
                  Damage: %d-%d, Luck: %d, Tithing points: %d
                  Defense chance increase: %d/%d, Hit chance increase: %d
                  Swing speed increase: %d, Weapon damage increase: %d
                  Lower reagent cost: %d, Spell damage increase: %d
                  Reflect physical damage: %d, Enhance potions: %d
                  Faster cast recovery: %d, Faster casting: %d, Lower mana cost: %d
                [Equipment and skills]
                  Backpack serial: %s
                  Equipped items (layer=serial): %s
                  Skills: %s
                """.formatted(
                target.getSerialId(), target.getSerialId(), target.getId(),
                target.getName(), target.getDisplayName(), target.getType(),
                target.getModelId(), target.getHue(), target.getRace(), target.getGender(),
                target.getX(), target.getY(), target.getZ(), target.getDirection(),
                target.isAlive(), target.isRunning(), target.isMounted(), target.isWarMode(),
                target.getStatus(), target.getNotoriety(),
                target.getHitpoints(), target.getMaxHitpoints(), target.getStamina(), target.getMaxStamina(),
                target.getMana(), target.getMaxMana(),
                target.getStrength(), target.getDexterity(), target.getIntelligence(), target.getStatCap(),
                target.getGold(), target.getWeight(), target.getMaxWeight(), target.getFollowers(), target.getMaxFollowers(),
                target.getPhysicalResist(), target.getMaxPhysicalResist(), target.getFireResist(), target.getMaxFireResist(),
                target.getColdResist(), target.getMaxColdResist(), target.getPoisonResist(), target.getMaxPoisonResist(),
                target.getEnergyResist(), target.getMaxEnergyResist(),
                target.getDamageMin(), target.getDamageMax(), target.getLuck(), target.getTithingPoints(),
                target.getDefenseChanceIncrease(), target.getMaxDefenseChanceIncrease(), target.getHitChanceIncrease(),
                target.getSwingSpeedIncrease(), target.getWeaponDamageIncrease(),
                target.getLowerReagentCost(), target.getSpellDamageIncrease(),
                target.getReflectPhysicalDamage(), target.getEnhancePotions(),
                target.getFasterCastRecovery(), target.getFasterCasting(), target.getLowerManaCost(),
                target.getBackpack(), target.getEquippedItems(), target.getSkills()));
        if (target instanceof UOPlayer player) {
            details.append("""
                    >>> PLAYER DETAILS <<<
                      Account ID: %s, Connected: %s
                      Ghost model: %d, Movement sequence: %d
                      Vendor session: %s
                    """.formatted(player.getAccountId(), player.isConnected(),
                    player.getGhostModelId(), player.movementSequence(), player.getVendorSession()));
        }
        if (target instanceof UONpc npc) {
            details.append("""
                    >>> NPC DETAILS <<<
                      Speech hue: %d, Speech font: %d
                      Behavior: %s
                      Roles: %s
                    """.formatted(npc.getSpeechHue(), npc.getSpeechFont(), npc.getBehavior(), npc.getRoles()));
        }
        details.append("=================================");
        log.info(details.toString());
    }

    private void logItem(UOItem item) {
        var details = new StringBuilder("\n========== ITEM INFO ==========\n");
        details.append("Class: ").append(item.getClass().getName()).append('\n');
        if (item.getClass() != UOItem.class) {
            details.append(">>> SUBCLASS: ").append(item.getClass().getSimpleName()).append(" <<<\n");
        }
        details.append("""
                [Identity]
                  Serial: %d (0x%08X), UUID: %s
                  Name: %s, Display name: %s
                  Model: %d (0x%04X), Hue: %d
                [Location and state]
                  Coordinates: (%d, %d, %d), Direction: %s
                  Current location: %s
                  Previous location: %s
                  Layer: %s, Movable: %s, Hidden: %s
                [Quantity and weight]
                  Amount: %d, Unit weight: %d
                [Flags]
                  %s
                [Template]
                  %s
                """.formatted(
                item.getSerialId(), item.getSerialId(), item.getId(),
                item.getName(), item.getDisplayName(),
                item.getModelId(), item.getModelId(), item.getHue(),
                item.getX(), item.getY(), item.getZ(), item.getDirection(),
                item.getCurrentLocation(), item.getPreviousLocation(),
                item.getLayer(), item.isMovable(), item.isHidden(),
                item.getAmount(), item.getUnitWeight(), item.getFlags(), item.getTemplate()));
        if (item instanceof UOContainer container) {
            var contents = container.getContainerItems();
            details.append("""
                    >>> CONTAINER DETAILS <<<
                      Gump ID: %d (0x%04X)
                      Item count: %d
                      Item serials: %s
                    """.formatted(container.getContainerGumpId(), container.getContainerGumpId(),
                    contents.size(), contents));
        }
        if (item instanceof UOCorpse corpse) {
            details.append("""
                    >>> CORPSE DETAILS <<<
                      Corpse ID: %d (0x%04X)
                      Equipped items (layer=serial): %s
                    """.formatted(corpse.getCorpseId(), corpse.getCorpseId(), corpse.getEquippedItems()));
        }
        details.append("===============================");
        log.info(details.toString());
    }
}
