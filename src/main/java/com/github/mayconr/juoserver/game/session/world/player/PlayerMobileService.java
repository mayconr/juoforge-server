package com.github.mayconr.juoserver.game.session.world.player;

import com.github.mayconr.juoserver.common.template.ItemTemplateRegistry;
import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.game.session.SessionOutbound;
import com.github.mayconr.juoserver.game.session.world.MobileFactory;
import com.github.mayconr.juoserver.game.session.world.SerialGenerator;
import com.github.mayconr.juoserver.game.session.world.item.ItemFactory;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;
import com.github.mayconr.juoserver.network.handler.AttributeKeys;
import com.github.mayconr.juoserver.network.packet.CreateCharacter;
import com.github.mayconr.juoserver.network.packet.LoginReject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RequiredArgsConstructor
public class PlayerMobileService {

    private final SerialGenerator serialGenerator;
    private final RealmStorage storage;
    private final ItemTemplateRegistry itemTemplateRegistry;

    public CompletableFuture<UOPlayer> createPlayer(CreateCharacter character, SessionOutbound outbound) {
        final var account = requireAccount(outbound);
        final var city = resolveCity(character, outbound);

        final var equippedItems = createInitialItems();
        final var skills = createInitialSkills(character);

        final var details = new PlayerDetails(
                account,
                "pass",
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
                        ? rejectCharacter(outbound, character.getCharacterName())
                        : persistNewPlayer(details)
                );
    }

    // -------------------------
    // Helpers
    // -------------------------

    private UOAccount requireAccount(SessionOutbound outbound) {
        final var account = outbound.attr().get(AttributeKeys.ACCOUNT_KEY);
        if (account == null) {
            throw new IllegalStateException("Account not found");
        }
        return account;
    }

    private UOCity resolveCity(CreateCharacter character, SessionOutbound outbound) {
        final var cities = outbound.attr()
                .remove(AttributeKeys.SESSION_CREATION_CONTEXT)
                .cities();

        final var city = cities.get((int) character.getLocationIndex());
        if (city == null) {
            throw new IllegalStateException("Invalid location index");
        }
        return city;
    }

    private List<UOItem> createInitialItems() {
        final var template = itemTemplateRegistry.get("backpack");
        if (template == null) {
            log.warn("Backpack template not found! Mobile will be created without backpack");
            return List.of();
        }

        return List.of(
                ItemFactory.createFromTemplate(
                        serialGenerator,
                        template,
                        new PointInTheWorld(0, 0, 0)
                )
        );
    }

    private List<SkillValue> createInitialSkills(CreateCharacter character) {
        // TODO load skill cap from ruleset
        return List.of(
                SkillValue.of(character.getSkill1(), character.getSkill1Value(), 100),
                SkillValue.of(character.getSkill2(), character.getSkill2Value(), 100),
                SkillValue.of(character.getSkill3(), character.getSkill3Value(), 100)
        );
    }

    private CompletableFuture<UOPlayer> rejectCharacter(SessionOutbound outbound, String name) {
        outbound.writeAndFlush(new LoginReject(LoginReject.Reason.CHAR_ALREADY_EXIST));
        return CompletableFuture.failedFuture(
                new IllegalStateException("Character already exists: " + name)
        );
    }

    private CompletableFuture<UOPlayer> persistNewPlayer(PlayerDetails details) {
        final var mobile = MobileFactory.createNewPlayer(serialGenerator, details);
        final int mobileSerialId = serialGenerator.getCurrentMobileSerial();
        final int itemSerialId = serialGenerator.getCurrentItemSerial();
        return storage.insertNewPlayer(mobileSerialId, itemSerialId, mobile);
    }

}
