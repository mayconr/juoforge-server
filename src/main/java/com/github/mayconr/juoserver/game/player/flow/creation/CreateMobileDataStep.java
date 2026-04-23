package com.github.mayconr.juoserver.game.player.flow.creation;

import com.github.mayconr.juoserver.game.flow.PlayerCreationFlowDefinition.PlayerCreationContext;
import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.game.world.SerialGenerator;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;

public class CreateMobileDataStep extends AbstractFlowStep<PlayerCreationContext> {

    private final SerialGenerator serialGenerator;
    private final RealmStorage storage;

    public CreateMobileDataStep(SerialGenerator serialGenerator,  RealmStorage storage) {
        super("CreateMobileData");
        this.serialGenerator = serialGenerator;
        this.storage = storage;
    }

    @Override
    public StepResult execute(PlayerCreationContext context) {
        final var character = context.getCharacter();
        final var bodyTemplate = context.getBodyTemplate();
        final var startingLocation = context.getStartingLocation();

        var data = new UOMobileData();
        // Player
        data.setAccountId(context.getAccount().getId());
        // Mobile
        data.setName(character.getCharacterName());
        data.setDisplayName(character.getCharacterName());
        data.setModelId(bodyTemplate.modelId());
        data.setGhostModelId(bodyTemplate.ghostModelId());
        data.setSerialId(serialGenerator.getNextMobile());
        data.setDirection(Direction.NORTH);
        data.setHue(character.getSkinColor());
        data.setStatus(CharacterStatus.NORMAL);
        data.setNotoriety(Notoriety.INNOCENT);
        data.setGender(bodyTemplate.gender());
        data.setRace(bodyTemplate.race());
        data.setType("P"); // Player
        data.setAlive(true);
        data.setLocation(startingLocation.getArea().getCenter());

        // Stats
        data.setStrength(character.getStrength());
        data.setIntelligence(character.getIntelligence());
        data.setDexterity(character.getDexterity());

        // Vitals
        data.setHitpoints(character.getStrength());
        data.setMaxHitpoints(character.getStrength());

        data.setMana(character.getIntelligence());
        data.setMaxMana(character.getIntelligence());

        data.setStamina(character.getDexterity());
        data.setMaxStamina(character.getDexterity());

        data.setSkills(context.getSkills());

        var equippedItems = context.getEquippedItems();
        for (Integer itemSerialId : equippedItems.values()) {
            storage.getItem(itemSerialId).ifPresent(item -> item.setCurrentLocation(new EquippedLocation(data.getSerialId())));
        }
        data.setEquippedItems(equippedItems);

        data.setPersistentAttrMap(new DefaultAttributeMap());
        context.setMobileData(data);

        return StepResult.success();
    }
}
