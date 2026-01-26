package com.github.mayconr.juoserver.game.session.world.player;

import com.github.mayconr.juoserver.common.template.ItemTemplateRegistry;
import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.game.session.SessionOutbound;
import com.github.mayconr.juoserver.game.session.world.SerialGenerator;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;
import com.github.mayconr.juoserver.game.session.world.item.ItemFactory;
import com.github.mayconr.juoserver.game.session.world.MobileFactory;
import com.github.mayconr.juoserver.network.packet.CreateCharacter;
import com.github.mayconr.juoserver.network.packet.LoginReject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RequiredArgsConstructor
public class PlayerMobileService {

    private final SerialGenerator serialGenerator;
    private final RealmStorage storage;
    private final ItemTemplateRegistry itemTemplateRegistry;

    public CompletableFuture<UOPlayer> createPlayer(UOAccount account, CreateCharacter character, SessionOutbound outbound) {
        final var template = itemTemplateRegistry.get("backpack");
        final var equippedItems = new ArrayList<UOItem>();
        if (template == null) {
            log.warn("Backpack template not found! Unable to equip mobile");
        } else {
            equippedItems.add(ItemFactory.createFromTemplate(serialGenerator, template, new PointInTheWorld(0,0,0)));
        }

        final var details = new PlayerDetails(account, "pass", character.getCharacterName(), equippedItems);

        return storage.mobileExists(character.getCharacterName())
                .thenCompose(exists->{
                    if (exists) {
                        outbound.writeAndFlush(new LoginReject(LoginReject.Reason.CHAR_ALREADY_EXIST));
                        throw new IllegalStateException("Username already exists " + character.getCharacterName());
                    } else {
                        return storage.createNewPlayer(MobileFactory.createNewPlayer(serialGenerator, details));
                    }
                });
    }

}
