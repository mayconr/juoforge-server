package com.github.mayconr.juoserver.game.player;

import com.github.mayconr.juoserver.JuoforgeConfiguration;
import com.github.mayconr.juoserver.game.item.ItemCreationRequest;
import com.github.mayconr.juoserver.game.item.exxception.ItemTemplateNotFoundException;
import com.github.mayconr.juoserver.game.model.SkillValue;
import com.github.mayconr.juoserver.game.model.UOAccount;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.model.policy.CreateCharacterPolicy;
import com.github.mayconr.juoserver.game.player.exception.PlayerNameAlreadyExistsException;
import com.github.mayconr.juoserver.game.world.MobileFactory;
import com.github.mayconr.juoserver.game.world.SerialGenerator;
import com.github.mayconr.juoserver.infrastructure.policy.PolicyService;
import com.github.mayconr.juoserver.infrastructure.region.RegionNode;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;
import com.github.mayconr.juoserver.network.packet.CreateCharacter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RequiredArgsConstructor
public class PlayerCreationHandler {

    public interface PlayerItemFactory {
        UOItem create(ItemCreationRequest request);
    }

    private final SerialGenerator serialGenerator;
    private final RealmStorage storage;
    private final JuoforgeConfiguration configuration;
    private final PolicyService policyService;
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

        return new PlayerDetails(
                account,
                character.getCharacterName(),
                buildInitialStatus(character),
                startingRegion.getArea().getCenter(),
                createStartKit(character),
                createInitialSkills(character)
        );
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
        System.out.println("hair "+character.getHairStyle()+" beard "+character.getBeardStyle());
        return List.of(
                ItemCreationRequest.byName(configuration.settings().mobile().backpack()).build(),
                ItemCreationRequest.byName("shirt").hue(character.getShirtColor()).build(),
                ItemCreationRequest.byName("pants").hue(character.getPantsColor()).build(),
                ItemCreationRequest.byName("shoes").build(),
                ItemCreationRequest.byModelId(character.getHairStyle()).hue(character.getHairColor()).build(),
                ItemCreationRequest.byModelId(character.getBeardStyle()).hue(character.getBeardColor()).build()
        );
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
                player.equipItem(item);
                storage.cacheItem(item);
            } catch (ItemTemplateNotFoundException exception) {
                log.warn("Unable to create item, due to: {}", exception.getMessage());
            }
        }
    }

}
