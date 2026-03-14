package com.github.mayconr.juoserver.game.player;

import com.github.mayconr.juoserver.JuoforgeConfiguration;
import com.github.mayconr.juoserver.game.item.ItemCreationRequest;
import com.github.mayconr.juoserver.game.item.exxception.ItemTemplateNotFoundException;
import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.game.model.policy.CreateCharacterPolicy;
import com.github.mayconr.juoserver.game.player.exception.PlayerNameAlreadyExistsException;
import com.github.mayconr.juoserver.game.player.template.BodyKey;
import com.github.mayconr.juoserver.game.player.template.BodyTemplate;
import com.github.mayconr.juoserver.game.player.template.StartkitTemplate;
import com.github.mayconr.juoserver.game.world.MobileFactory;
import com.github.mayconr.juoserver.game.world.SerialGenerator;
import com.github.mayconr.juoserver.infrastructure.policy.PolicyService;
import com.github.mayconr.juoserver.infrastructure.region.RegionNode;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;
import com.github.mayconr.juoserver.infrastructure.template.TemplateRegistry;
import com.github.mayconr.juoserver.network.packet.CreateCharacter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;

import static com.github.mayconr.juoserver.game.item.ItemCreationRequest.byModelId;
import static com.github.mayconr.juoserver.game.item.ItemCreationRequest.byName;

@Slf4j
@RequiredArgsConstructor
public class PlayerCreationHandler {

    public static final int NO_SKILL_ASSIGNED = -1;

    public interface PlayerItemFactory {
        UOItem create(ItemCreationRequest request);
    }

    private final SerialGenerator serialGenerator;
    private final RealmStorage storage;
    private final JuoforgeConfiguration configuration;
    private final PolicyService policyService;
    private final TemplateRegistry<BodyKey, BodyTemplate> bodyTemplateRegistry;
    private final TemplateRegistry<Integer, StartkitTemplate> startkitTemplateRegistry;

    private PlayerItemFactory itemFactory;

    public void initialize(PlayerItemFactory playerItemFactory) {
        this.itemFactory = playerItemFactory;
    }

    public CompletableFuture<UOPlayer> createPlayerMobile(CreateCharacter character, Map<Integer, RegionNode> startingLocations, UOAccount account) {
        final var playerName = character.getCharacterName();
        final var playerDetails = buildPlayerDetails(character, startingLocations, account);

        return storage.mobileExists(playerName)
                .thenCompose(exists -> exists
                        ? CompletableFuture.failedStage(new PlayerNameAlreadyExistsException())
                        : createAndSavePlayer(playerDetails)
                );
    }

    private PlayerDetails buildPlayerDetails(CreateCharacter character, Map<Integer, RegionNode> startingLocations, UOAccount account) {
        final var startingRegion = resolveStartingLocation(character, startingLocations);
        try {
            final var bodyTemplate = bodyTemplateRegistry.get(new BodyKey(character.getGender(), character.getRace()))
                    .getFirst();

            return new PlayerDetails(
                    account,
                    character.getCharacterName(),
                    character.getSkinColor(),
                    bodyTemplate,
                    buildInitialStatus(character),
                    startingRegion.getArea().getCenter(),
                    createStartKit(character),
                    createInitialSkills(character)
            );
        } catch (NoSuchElementException exception) {
            throw new IllegalStateException("Body template not found for character " + character, exception);
        }

    }

    private PlayerDetails.Status buildInitialStatus(CreateCharacter character) {
        return new PlayerDetails.Status(
                character.getStrength(),
                character.getDexterity(),
                character.getIntelligence()
        );
    }

    private RegionNode resolveStartingLocation(CreateCharacter character, Map<Integer, RegionNode> startingLocations) {
        final var startingRegion = startingLocations.get((int) character.getLocationIndex());
        if (startingRegion == null) {
            throw new IllegalStateException("Invalid location index");
        }
        return startingRegion;
    }

    private List<ItemCreationRequest> createStartKit(CreateCharacter character) {
        var settings = configuration.settings();

        var kit = new ArrayList<ItemCreationRequest>();
        kit.add(byName("shirt").hue(character.getShirtColor()).build());
        kit.add(byName("pants").hue(character.getPantsColor()).build());
        kit.add(byName("shoes").build());
        kit.add(byModelId(character.getHairStyle()).hue(character.getHairColor()).build());
        kit.add(byModelId(character.getBeardStyle()).hue(character.getBeardColor()).build());
        kit.add(byName(settings.mobile().backpack()).build());

        for (var template : startkitTemplateRegistry.get(NO_SKILL_ASSIGNED)) {
            kit.add(byName(template.item()).amount(template.amount()).build());
        }
        for (var template : startkitTemplateRegistry.get(character.getSkill1())) {
            kit.add(byName(template.item()).amount(template.amount()).build());
        }
        for (var template : startkitTemplateRegistry.get(character.getSkill2())) {
            kit.add(byName(template.item()).amount(template.amount()).build());
        }
        for (var template : startkitTemplateRegistry.get(character.getSkill3())) {
            kit.add(byName(template.item()).amount(template.amount()).build());
        }

        return List.copyOf(kit);
    }

    private List<SkillValue> createInitialSkills(CreateCharacter character) {
        return List.of(
                SkillValue.of(character.getSkill1(), character.getSkill1Value(), configuration.settings().skills().cap()),
                SkillValue.of(character.getSkill2(), character.getSkill2Value(), configuration.settings().skills().cap()),
                SkillValue.of(character.getSkill3(), character.getSkill3Value(), configuration.settings().skills().cap())
        );
    }

    private CompletableFuture<UOPlayer> createAndSavePlayer(PlayerDetails details) {
        final var policyResult = policyService.evaluate(CreateCharacterPolicy.class, new CreateCharacterPolicy(details));
        if (!policyResult.allowed()) {
            return CompletableFuture.failedFuture(new IllegalStateException("Player creation aborted by policy. Reason: " + policyResult.reason()));
        }

        final var player = MobileFactory.createNewPlayer(serialGenerator, details);
        storage.cacheMobile(player);

        createAndEquipStarterItems(player, details.startkit());

        final int mobileSerialId = serialGenerator.getCurrentMobile();
        final int itemSerialId = serialGenerator.getCurrentItem();

        return storage.insertPlayerMobile(mobileSerialId, itemSerialId, player);
    }

    private void createAndEquipStarterItems(UOPlayer player, List<ItemCreationRequest> starterItems) {
        for (var itemRequest : starterItems) {
            try {
                final var item = itemFactory.create(itemRequest);
                if (item == null) {
                    log.warn("Unable to create item: {}", itemRequest);
                    continue;
                }
                if (item.hasFlag(ItemFlag.WEARABLE)) {
                    player.equipItem(item);
                } else {
                    player.addItemToContainer(item);
                }
                storage.cacheItem(item);
            } catch (ItemTemplateNotFoundException exception) {
                log.warn("Unable to create item, due to: {}", exception.getMessage());
            }
        }
    }

}
