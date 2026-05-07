package com.github.mayconr.juoserver;

import com.github.mayconr.juoserver.game.economy.PricingStrategy;
import com.github.mayconr.juoserver.game.economy.ScarcityBasedPricingStrategy;
import com.github.mayconr.juoserver.game.item.trigger.ItemUseTrigger;
import com.github.mayconr.juoserver.game.wallet.PhisycalGoldWallet;
import com.github.mayconr.juoserver.game.wallet.Wallet;
import com.github.mayconr.juoserver.game.world.World;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventRegistry;
import com.github.mayconr.juoserver.infrastructure.eventbus.GameEvent;
import com.github.mayconr.juoserver.infrastructure.storage.AccountStorage;
import com.github.mayconr.juoserver.infrastructure.storage.ItemStorage;
import com.github.mayconr.juoserver.infrastructure.storage.MobileStorage;
import com.github.mayconr.juoserver.infrastructure.template.JsonTemplateLoaderNew;
import com.github.mayconr.juoserver.infrastructure.template.TemplateLoader;
import lombok.RequiredArgsConstructor;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

@RequiredArgsConstructor
public class DefaultWorldCfg implements WorldCfg {

    // ===== Economy =====
    private Function<World, Wallet> wallet = PhisycalGoldWallet::new;
    private Supplier<PricingStrategy> pricingStrategy = ScarcityBasedPricingStrategy::new;

    // ===== Item trigger ====
    private final List<Function<ServerRuntime, ItemUseTrigger>> itemTriggerList = new ArrayList<>();

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
    public void addItemTrigger(Function<ServerRuntime, ItemUseTrigger> triggerFactory) {
        itemTriggerList.add(triggerFactory);
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
    public List<Function<ServerRuntime, ItemUseTrigger>> itemTriggerList() {
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
