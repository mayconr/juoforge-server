package com.github.mayconr.juoserver;
import com.github.mayconr.juoserver.infrastructure.storage.WorldSaveListener;

import com.github.mayconr.juoserver.game.GamePlaySettings;
import com.github.mayconr.juoserver.game.loot.LootPolicy;
import com.github.mayconr.juoserver.game.ui.policy.DoubleClickPolicy;

import com.github.mayconr.juoserver.game.GamePlaySettings.Ai;
import com.github.mayconr.juoserver.game.ai.policy.AIActivationPolicy;
import com.github.mayconr.juoserver.game.ai.registry.AIContextFactory;
import java.util.Map;
import com.github.mayconr.juoserver.game.ai.definition.AIFlowContext;
import com.github.mayconr.juoserver.game.ai.policy.AISpeechPolicy;
import com.github.mayconr.juoserver.game.economy.PricingStrategy;
import com.github.mayconr.juoserver.game.combat.progression.CombatSkillGainPolicy;
import com.github.mayconr.juoserver.game.item.trigger.ItemUseTrigger;
import com.github.mayconr.juoserver.game.spell.trigger.SpellCastTrigger;
import com.github.mayconr.juoserver.game.wallet.Wallet;
import com.github.mayconr.juoserver.game.world.World;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventRegistry;
import com.github.mayconr.juoserver.infrastructure.eventbus.GameEvent;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractContext;
import com.github.mayconr.juoserver.infrastructure.flow.Flow;
import com.github.mayconr.juoserver.infrastructure.storage.AccountStorage;
import com.github.mayconr.juoserver.infrastructure.storage.ItemStorage;
import com.github.mayconr.juoserver.infrastructure.storage.MobileStorage;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public interface WorldCfg {

    /** Replaces double-click authorization; the default allows all requests. */
    void doubleClickPolicy(Supplier<DoubleClickPolicy> factory);

    Supplier<DoubleClickPolicy> doubleClickPolicy();

    void lootPolicy(Supplier<LootPolicy> factory);

    Supplier<LootPolicy> lootPolicy();

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

    /** Replaces the policy that decides whether an AI session may execute. */
    void aiActivationPolicy(Supplier<AIActivationPolicy> factory);

    Supplier<AIActivationPolicy> aiActivationPolicy();

    /** Replaces the policy that decides which speech events an AI session accepts. */
    void aiSpeechPolicy(Supplier<AISpeechPolicy> factory);

    Supplier<AISpeechPolicy> aiSpeechPolicy();

    /** Registers a shard flow factory. Context types must be unique across core and shard flows. */
    <T extends AbstractContext> void addFlow(
            Class<T> contextType,
            Function<ServerRuntime, Flow<T>> factory);

    List<ShardFlowRegistration<?>> flowList();

    /** Registers an AI factory evaluated with the runtime after shard flows are registered. */
    void addAi(String type, Function<ServerRuntime, AIContextFactory<?>> factory);

    /**
     * Configures an AI registration and its context flow together.
     * Both factories receive the runtime; the flow is registered before the AI.
     * Context types must be unique across core and shard flows.
     */
    <T extends AIFlowContext> void addAi(AIRegistration<T> registration);

    Map<String, Function<ServerRuntime, AIContextFactory<?>>> aiList();

    /** Replaces the default weapon-skill and Tactics gain policy for combat swings. */
    void combatSkillGainPolicy(Supplier<CombatSkillGainPolicy> factory);

    Supplier<CombatSkillGainPolicy> combatSkillGainPolicy();

    // ===== Economy =====
    void wallet(Function<World, Wallet> walletFactory);

    void pricingStrategy(Supplier<PricingStrategy> pricingStrategyFactory);

    // Item use trigger
    void addItemTrigger(String script, Function<ServerRuntime, ItemUseTrigger> triggerFactory);

    /** Registers a spell trigger factory. The first supporting trigger handles each request. */
    void addSpellTrigger(Function<ServerRuntime, SpellCastTrigger> triggerFactory);

    // Events
    <T extends GameEvent> void addEventListener(Function<ServerRuntime, EventRegistry<T>> registry);

    /** Creates and registers additional world-save work after runtime initialization, before the game loop starts. */
    void addWorldSaveListener(Function<ServerRuntime, WorldSaveListener> factory);

    List<Function<ServerRuntime, WorldSaveListener>> worldSaveListenerList();

    // Templates
    <T, K> void addCustomTemplate(String templateName, Class<T> templateClass, Function<T, K> keyExtractor, Path file);

    // ===== Storage =====
    void mobileStorage(MobileStorage mobileStorage);

    void itemStorage(ItemStorage itemStorage);

    void accountStorage(AccountStorage accountStorage);

    // ===== Read methods (optional, for engine use) =====
    Function<World, Wallet> wallet();

    Supplier<PricingStrategy> pricingStrategy();

    Map<String, Function<ServerRuntime, ItemUseTrigger>> itemTriggerList();

    List<Function<ServerRuntime, SpellCastTrigger>> spellTriggerList();

    List<Function<ServerRuntime, EventRegistry<GameEvent>>> eventListenerList();

    <K, V> List<DefaultWorldCfg.TemplateData<K, V>> templateList();

    MobileStorage mobileStorage();

    ItemStorage itemStorage();

    AccountStorage accountStorage();

}
