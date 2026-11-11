package com.github.mayconr.juoserver;

import com.github.mayconr.juoserver.game.GamePlaySettings;
import com.github.mayconr.juoserver.game.GamePlaySettings.Ai;
import com.github.mayconr.juoserver.infrastructure.storage.account.AccountStorage;
import com.github.mayconr.juoserver.infrastructure.storage.property.PropertyStorage;
import com.github.mayconr.juoserver.infrastructure.storage.item.ItemStorage;
import com.github.mayconr.juoserver.infrastructure.storage.mobile.MobileStorage;

/** Configures shard content, settings and storage and registers modules. */
public interface WorldConfigurator extends ModuleConfiguration {
    /** Registers a module immediately, passing this same world configuration to it. */
    void addModule(UOModule module);

    void content(WorldContent content);

    WorldContent content();

    /** Configures server timing and client data files in code. */
    void serverSettings(ServerSettings settings);

    ServerSettings serverSettings();

    /** Replaces gameplay defaults with rules configured in code. */
    void gameplaySettings(GamePlaySettings settings);

    GamePlaySettings gameplaySettings();

    /** Combines gameplay rules with the server parameters supplied by the shard. */
    default GamePlaySettings settings() {
        return serverSettings().applyTo(gameplaySettings());
    }

    /** AI timing configured by the shard. */
    default Ai ai() {
        return new Ai(serverSettings().aiUpdateIntervalSeconds());
    }

    void propertyStorage(PropertyStorage storage);

    PropertyStorage propertyStorage();

    // ===== Storage =====
    void mobileStorage(MobileStorage mobileStorage);

    void itemStorage(ItemStorage itemStorage);

    void accountStorage(AccountStorage accountStorage);

    MobileStorage mobileStorage();

    ItemStorage itemStorage();

    AccountStorage accountStorage();

}
