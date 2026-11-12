package com.github.mayconr.juoserver;

import com.github.mayconr.juoserver.game.GamePlaySettings;
import com.github.mayconr.juoserver.game.ai.definition.AIFlowContext;
import com.github.mayconr.juoserver.game.ai.policy.AIActivationPolicy;
import com.github.mayconr.juoserver.game.ai.policy.AISpeechPolicy;
import com.github.mayconr.juoserver.game.ai.policy.NearbyPlayerActivationPolicy;
import com.github.mayconr.juoserver.game.ai.registry.AIContextFactory;
import com.github.mayconr.juoserver.game.combat.progression.CombatSkillGainPolicy;
import com.github.mayconr.juoserver.game.combat.progression.DefaultCombatSkillGainPolicy;
import com.github.mayconr.juoserver.game.economy.PricingStrategy;
import com.github.mayconr.juoserver.game.economy.ScarcityBasedPricingStrategy;
import com.github.mayconr.juoserver.game.item.trigger.ItemUseTrigger;
import com.github.mayconr.juoserver.game.loot.LootPolicy;
import com.github.mayconr.juoserver.game.spell.trigger.SpellCastTrigger;
import com.github.mayconr.juoserver.game.ui.contextmenu.ContextMenuEntry;
import com.github.mayconr.juoserver.game.ui.policy.DoubleClickPolicy;
import com.github.mayconr.juoserver.game.wallet.PhisycalGoldWallet;
import com.github.mayconr.juoserver.game.wallet.Wallet;
import com.github.mayconr.juoserver.game.world.World;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventRegistry;
import com.github.mayconr.juoserver.infrastructure.eventbus.GameEvent;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractContext;
import com.github.mayconr.juoserver.infrastructure.flow.Flow;
import com.github.mayconr.juoserver.infrastructure.storage.account.AccountStorage;
import com.github.mayconr.juoserver.infrastructure.storage.item.ItemStorage;
import com.github.mayconr.juoserver.infrastructure.storage.mobile.MobileStorage;
import com.github.mayconr.juoserver.infrastructure.storage.property.PropertyStorage;
import com.github.mayconr.juoserver.infrastructure.template.JsonTemplateLoaderNew;
import com.github.mayconr.juoserver.infrastructure.template.TemplateLoader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static java.util.Objects.*;

@RequiredArgsConstructor
@Slf4j
public class DefaultWorldConfigurator implements WorldConfigurator {

    private final List<Consumer<ServerRuntime>> initializers = new ArrayList<>();

    @Override
    public void addInitializer(Consumer<ServerRuntime> initializer) {
        initializers.add(requireNonNull(initializer, "Module initializer is required"));
    }

    @Override
    public List<Consumer<ServerRuntime>> initializerList() {
        return List.copyOf(initializers);
    }

    @Override
    public void addModule(UOModule module) {
        requireNonNull(module, "Module is required").register(this);
        log.info("Module registered: {}", module.getClass().getName());
    }

    private final List<String> messageBundles = new ArrayList<>();

    @Override
    public void addMessageBundle(String baseName) {
        if (baseName == null || baseName.isBlank()) {
            throw new IllegalArgumentException("Message bundle name is required");
        }
        if (!messageBundles.contains(baseName)) {
            messageBundles.add(baseName);
        }
    }

    @Override
    public List<String> messageBundles() {
        return List.copyOf(messageBundles);
    }

    private Supplier<DoubleClickPolicy> doubleClickPolicy = DoubleClickPolicy::allowAll;

    @Override
    public void doubleClickPolicy(Supplier<DoubleClickPolicy> factory) {
        this.doubleClickPolicy = requireNonNull(factory, "Double click policy factory is required");
    }

    @Override
    public Supplier<DoubleClickPolicy> doubleClickPolicy() {
        return doubleClickPolicy;
    }

    private Supplier<LootPolicy> lootPolicy =
            LootPolicy::none;

    @Override
    public void lootPolicy(Supplier<LootPolicy> factory) {
        this.lootPolicy = requireNonNull(factory);
    }

    @Override
    public Supplier<LootPolicy> lootPolicy() {
        return lootPolicy;
    }

    private ServerSettings serverSettings;

    @Override
    public void serverSettings(ServerSettings settings) {
        this.serverSettings = requireNonNull(settings, "Server settings are required");
    }

    @Override
    public ServerSettings serverSettings() {
        return requireNonNull(serverSettings, "Shard must configure server settings");
    }

    private GamePlaySettings gameplaySettings =
            new GamePlaySettings();

    @Override
    public void gameplaySettings(GamePlaySettings settings) {
        this.gameplaySettings = requireNonNull(settings, "Gameplay settings are required");
    }

    @Override
    public GamePlaySettings gameplaySettings() {
        return gameplaySettings;
    }

    private WorldContent content;

    @Override
    public void content(WorldContent content) {
        this.content = requireNonNull(content, "World content is required");
    }

    @Override
    public WorldContent content() {
        return requireNonNull(content, "Shard must configure world content");
    }

    private Supplier<AIActivationPolicy> aiActivationPolicy =
            () -> new NearbyPlayerActivationPolicy(gameplaySettings().world().visibility().range());
    private Supplier<AISpeechPolicy> aiSpeechPolicy =
            () -> AISpeechPolicy.nearbyVendor(gameplaySettings().world().visibility().range());

    @Override
    public void aiActivationPolicy(Supplier<AIActivationPolicy> factory) {
        this.aiActivationPolicy = requireNonNull(factory, "AI activation policy factory is required");
    }

    @Override
    public Supplier<AIActivationPolicy> aiActivationPolicy() {
        return aiActivationPolicy;
    }

    @Override
    public void aiSpeechPolicy(Supplier<AISpeechPolicy> factory) {
        this.aiSpeechPolicy = requireNonNull(factory, "AI speech policy factory is required");
    }

    @Override
    public Supplier<AISpeechPolicy> aiSpeechPolicy() {
        return aiSpeechPolicy;
    }

    private final List<ShardFlowRegistration<?>> flowList = new ArrayList<>();
    private final Map<String, Function<ServerRuntime, AIContextFactory<?>>> aiList = new HashMap<>();

    @Override
    public void addAi(String type, Function<ServerRuntime, AIContextFactory<?>> factory) {
        requireNonNull(type, "AI type is required");
        requireNonNull(factory, "AI context factory is required");
        if (type.isBlank()) throw new IllegalArgumentException("AI type must not be blank");
        if (aiList.putIfAbsent(type, factory) != null) {
            throw new IllegalArgumentException("AI type already configured: " + type);
        }
    }

    @Override
    public <T extends AIFlowContext> void addAi(AIRegistration<T> registration) {
        requireNonNull(registration, "AI registration is required");
        var type = registration.type();
        if (aiList.containsKey(type)) throw new IllegalArgumentException("AI type already configured: " + type);
        addFlow(registration.contextType(), registration.flowFactory());
        addAi(type, runtime -> registration.factory().apply(runtime));
    }

    @Override
    public Map<String, Function<ServerRuntime, AIContextFactory<?>>> aiList() {
        return Map.copyOf(aiList);
    }

    @Override
    public <T extends AbstractContext> void addFlow(
            Class<T> contextType,
            Function<ServerRuntime, Flow<T>> factory) {
        var registration = new ShardFlowRegistration<>(contextType, factory);
        if (flowList.stream().anyMatch(entry -> entry.contextType().equals(contextType))) {
            throw new IllegalArgumentException("Flow already configured for " + contextType.getName());
        }
        flowList.add(registration);
    }

    @Override
    public List<ShardFlowRegistration<?>> flowList() {
        return List.copyOf(flowList);
    }

    private Supplier<CombatSkillGainPolicy> combatSkillGainPolicy = DefaultCombatSkillGainPolicy::new;

    @Override
    public void combatSkillGainPolicy(Supplier<CombatSkillGainPolicy> factory) {
        this.combatSkillGainPolicy = requireNonNull(factory);
    }

    @Override
    public Supplier<CombatSkillGainPolicy> combatSkillGainPolicy() {
        return combatSkillGainPolicy;
    }

    // ===== Economy =====
    private Function<World, Wallet> wallet = PhisycalGoldWallet::new;
    private Supplier<PricingStrategy> pricingStrategy = ScarcityBasedPricingStrategy::new;

    private final List<Function<ServerRuntime, ContextMenuEntry>> contextMenuEntries = new ArrayList<>();

    @Override
    public void addContextMenuEntry(Function<ServerRuntime, ContextMenuEntry> factory) {
        contextMenuEntries.add(requireNonNull(factory, "Context menu factory is required"));
    }

    @Override
    public List<Function<ServerRuntime, ContextMenuEntry>> contextMenuEntries() {
        return List.copyOf(contextMenuEntries);
    }

    // ===== Item trigger ====
    private final Map<String, Function<ServerRuntime, ItemUseTrigger>> itemTriggerList = new LinkedHashMap<>();
    private final List<Function<ServerRuntime, SpellCastTrigger>> spellTriggerList = new ArrayList<>();

    // Event listener
    private final List<Function<ServerRuntime, EventRegistry<GameEvent>>> eventListenerList = new ArrayList<>();

    // Templates
    private final Map<String, TemplateData<?,?>> templateLoaderList = new HashMap<>();

    // ===== Storage =====
    private MobileStorage mobileStorage;
    private ItemStorage itemStorage;
    private AccountStorage accountStorage;

    // ===== Economy =====

    @Override
    public void wallet(Function<World, Wallet> walletFactory) {
        this.wallet = walletFactory;
    }

    @Override
    public void pricingStrategy(Supplier<PricingStrategy> pricingStrategyFactory) {
        this.pricingStrategy = pricingStrategyFactory;
    }

    // ===== Item Triggers =====

    @Override
    public void addItemTrigger(String script, Function<ServerRuntime, ItemUseTrigger> triggerFactory) {
        if (script == null || script.isBlank()) throw new IllegalArgumentException("Item script name is required");
        requireNonNull(triggerFactory, "Item trigger factory is required");
        if (itemTriggerList.putIfAbsent(script, triggerFactory) != null) {
            throw new IllegalArgumentException("Item script already registered: " + script);
        }
    }

    @Override
    public void addSpellTrigger(Function<ServerRuntime, SpellCastTrigger> triggerFactory) {
        spellTriggerList.add(requireNonNull(triggerFactory, "Spell trigger factory is required"));
    }

    @Override
    public List<Function<ServerRuntime, SpellCastTrigger>> spellTriggerList() {
        return List.copyOf(spellTriggerList);
    }

    @Override
    public <T extends GameEvent> void addEventListener(Function<ServerRuntime, EventRegistry<T>> registryFactory) {
        eventListenerList.add((Function) registryFactory);
    }

    // ===== Template =======
    @Override
    public <T, K> void addCustomTemplate(String templateName, Class<T> templateClass, Function<T, K> keyExtractor, Path path) {
        if (templateLoaderList.containsKey(templateName)) {
            throw new IllegalArgumentException("Template " + templateName + " already exists");
        }
        templateLoaderList.put(templateName, new TemplateData<>(templateName, templateClass, keyExtractor, new JsonTemplateLoaderNew<>(path, templateClass)));
    }

    private PropertyStorage propertyStorage;

    @Override
    public <T, K> void addCustomTemplate(String templateName, Class<T> templateClass,
                                         Function<T, K> keyExtractor, List<T> templates) {
        if (templateLoaderList.containsKey(templateName)) {
            throw new IllegalArgumentException("Template " + templateName + " already exists");
        }
        var values = List.copyOf(templates);
        TemplateLoader<T> loader = new TemplateLoader<>() {
            @Override
            public List<T> loadAll() {
                return values;
            }
        };
        templateLoaderList.put(templateName, new TemplateData<>(templateName, templateClass, keyExtractor, loader));
    }

    @Override
    public void propertyStorage(PropertyStorage storage) {
        this.propertyStorage = requireNonNull(storage, "Property storage is required");
    }

    @Override
    public PropertyStorage propertyStorage() {
        return propertyStorage;
    }

    // ===== Storage =====

    @Override
    public void mobileStorage(MobileStorage mobileStorage) {
        this.mobileStorage = mobileStorage;
    }

    @Override
    public void itemStorage(ItemStorage itemStorage) {
        this.itemStorage = itemStorage;
    }

    @Override
    public void accountStorage(AccountStorage accountStorage) {
        this.accountStorage = accountStorage;
    }

    // ===== Read methods =====

    @Override
    public Function<World, Wallet> wallet() {
        return wallet;
    }

    @Override
    public Supplier<PricingStrategy> pricingStrategy() {
        return pricingStrategy;
    }

    @Override
    public Map<String, Function<ServerRuntime, ItemUseTrigger>> itemTriggerList() {
        return itemTriggerList;
    }

    @Override
    public List<Function<ServerRuntime, EventRegistry<GameEvent>>> eventListenerList() {
        return eventListenerList;
    }

    @Override
    public <V, K> List<TemplateData<V, K>> templateList() {
        return (List) List.copyOf(templateLoaderList.values());
    }

    @Override
    public MobileStorage mobileStorage() {
        return mobileStorage;
    }

    @Override
    public ItemStorage itemStorage() {
        return itemStorage;
    }

    @Override
    public AccountStorage accountStorage() {
        return accountStorage;
    }

    public record TemplateData<V, K>(String templateName, Class<V> templateClass, Function<V, K> keyExtractor, TemplateLoader<V> templateLoader) {}
}
