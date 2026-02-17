package com.github.mayconr.juoserver.game.world.module.player;

import com.github.mayconr.juoserver.ServerProperties;
import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.game.model.policy.CreateCharacterPolicy;
import com.github.mayconr.juoserver.infrastructure.policy.PolicyService;
import com.github.mayconr.juoserver.game.player.SessionOutbound;
import com.github.mayconr.juoserver.game.world.MobileFactory;
import com.github.mayconr.juoserver.game.world.SerialGenerator;
import com.github.mayconr.juoserver.game.item.ItemFactory;
import com.github.mayconr.juoserver.game.item.template.ItemTemplateRegistry;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;
import com.github.mayconr.juoserver.network.packet.CreateCharacter;
import com.github.mayconr.juoserver.network.packet.LoginReject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RequiredArgsConstructor
public class PlayerCreationHandler {

    private final SerialGenerator serialGenerator;
    private final RealmStorage storage;
    private final ItemTemplateRegistry itemTemplateRegistry;
    private final ServerProperties properties;
    private final PolicyService policyService;

    public CompletableFuture<UOPlayer> createNewPlayer(CreateCharacter character, Map<Integer, UOCity> cities, UOAccount account) {

        final var city = resolveCity(character, cities);

        final var equippedItems = createInitialItems(character);
        final var skills = createInitialSkills(character);

        final var details = new PlayerDetails(
                account,
                character.getCharacterName(),
                new PlayerDetails.Status(
                        character.getStrength(),
                        character.getDexterity(),
                        character.getIntelligence()
                ),
                city.startingLocation(),
                equippedItems,
                skills
        );

        return storage.mobileExists(character.getCharacterName())
                .thenCompose(exists -> exists
                        ? CompletableFuture.failedStage(new IllegalArgumentException("Player already exists"))
                        : persistNewPlayer(details)
                );
    }

    // -------------------------
    // Helpers
    // -------------------------

    private UOCity resolveCity(CreateCharacter character, Map<Integer,UOCity> cities) {
        final var city = cities.get((int) character.getLocationIndex());
        if (city == null) {
            throw new IllegalStateException("Invalid location index");
        }
        return city;
    }

    private List<UOItem> createInitialItems(CreateCharacter character) {
        final var backpack = itemTemplateRegistry.get(properties.mobile().backpack());
        final var shirt = itemTemplateRegistry.get("shirt");
        final var pants = itemTemplateRegistry.get("pants");
        final var shoes = itemTemplateRegistry.get("shoes");
        final var startkit = new ArrayList<UOItem>();

        if (backpack != null) {
            startkit.add(ItemFactory.createFromTemplate(serialGenerator, backpack));
        }
        if (shirt != null) {
            var shirtItem = ItemFactory.createFromTemplate(serialGenerator, shirt);
            shirtItem.setHue(character.getShirtColor());
            startkit.add(shirtItem);
        }
        if (pants != null) {
            var pantsItem = ItemFactory.createFromTemplate(serialGenerator, pants);
            pantsItem.setHue(character.getPantsColor());
            startkit.add(pantsItem);
        }
        if (shoes != null) {
            startkit.add(ItemFactory.createFromTemplate(serialGenerator, shoes));
        }

        // Create beard
        if (character.getBeardStyle() > 0) {
            createHair(character.getHairStyle(), character.getHairColor())
                .ifPresent(startkit::add);
        }

        // Create hair
        if (character.getBeardStyle() > 0) {
            createHair(character.getBeardStyle(), character.getBeardColor())
                .ifPresent(startkit::add);
        }

        return startkit;
    }

    private Optional<UOItem> createHair(int style, int hue) {
        return itemTemplateRegistry.get(style)
                .stream()
                .findFirst()
                .map(template->ItemFactory.createFromTemplate(serialGenerator, template))
                .map(beard->{
                    beard.setHue(hue);
                    return beard;
                });
    }

    private List<SkillValue> createInitialSkills(CreateCharacter character) {
        return List.of(
                SkillValue.of(character.getSkill1(), character.getSkill1Value(), properties.skills().cap()),
                SkillValue.of(character.getSkill2(), character.getSkill2Value(), properties.skills().cap()),
                SkillValue.of(character.getSkill3(), character.getSkill3Value(), properties.skills().cap())
        );
    }

    private CompletableFuture<UOPlayer> rejectCharacter(SessionOutbound outbound, String name) {
        outbound.writeAndFlush(new LoginReject(LoginReject.Reason.CHAR_ALREADY_EXIST));
        return CompletableFuture.failedFuture(
                new IllegalStateException("Character already exists: " + name)
        );
    }

    private CompletableFuture<UOPlayer> persistNewPlayer(PlayerDetails details) {
        final var result = policyService.evaluate(CreateCharacterPolicy.class, new CreateCharacterPolicy(details));
        if (result.allowed()) {
            final var mobile = MobileFactory.createNewPlayer(serialGenerator, details);
            final int mobileSerialId = serialGenerator.getCurrentMobileSerial();
            final int itemSerialId = serialGenerator.getCurrentItemSerial();
            return storage.insertNewPlayer(mobileSerialId, itemSerialId, mobile);
        }
        return CompletableFuture.failedFuture(new IllegalStateException("Player creation aborted by policy. Reason: "+result.reason()));
    }

}
