package com.github.mayconr.juoserver;

import com.github.mayconr.juoserver.game.GamePlaySettings;
import com.github.mayconr.juoserver.game.GamePlaySettings.Ai;
import com.github.mayconr.juoserver.infrastructure.storage.account.AccountStorage;
import com.github.mayconr.juoserver.infrastructure.storage.property.PropertyStorage;
import com.github.mayconr.juoserver.infrastructure.storage.item.ItemStorage;
import com.github.mayconr.juoserver.infrastructure.storage.mobile.MobileStorage;
import com.github.mayconr.juoserver.infrastructure.template.JsonTemplateLoaderNew;

import java.nio.file.Path;
import java.util.List;

import static java.util.Objects.requireNonNull;

/** Configures shard content, settings and storage and registers modules. */
public interface WorldConfigurator extends ModuleConfiguration {
    /** Registers a module immediately using the same dependency checks as {@link #requireModule(UOModule)}. */
    void addModule(UOModule module);

    /**
     * Loads templates from a JSON or YAML file, or from every such file under a directory,
     * and returns them as an immutable list without registering them.
     * JSON files contain an array; YAML files contain an object or a list.
     *
     * @throws IllegalStateException when the path cannot be read or a template cannot be parsed or constructed
     */
    default <T> List<T> loadTemplates(Path path, Class<T> templateClass) {
        requireNonNull(path, "Template path is required");
        requireNonNull(templateClass, "Template class is required");
        return new JsonTemplateLoaderNew<>(path, templateClass).loadAll();
    }

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
