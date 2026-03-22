package com.github.mayconr.juoserver;

import com.github.mayconr.juoserver.game.ai.AI;
import com.github.mayconr.juoserver.game.ai.BehaviorProfile;
import com.github.mayconr.juoserver.game.economy.PricingStrategy;
import com.github.mayconr.juoserver.game.item.trigger.ItemUseTrigger;
import com.github.mayconr.juoserver.game.wallet.Wallet;
import com.github.mayconr.juoserver.game.world.World;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventRegistry;
import com.github.mayconr.juoserver.infrastructure.eventbus.GameEvent;
import com.github.mayconr.juoserver.infrastructure.storage.AccountStorage;
import com.github.mayconr.juoserver.infrastructure.storage.ItemStorage;
import com.github.mayconr.juoserver.infrastructure.storage.MobileStorage;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public interface WorldCfg {

    // ===== Economy =====
    void wallet(Function<World, Wallet> walletFactory);

    void pricingStrategy(Supplier<PricingStrategy> pricingStrategyFactory);

    // ===== AI =====
    void addNpcAI(AI ai);

    void addBehaviorProfile(BehaviorProfile profile);

    // Item use trigger
    void addItemTrigger(Function<ServerRuntime, ItemUseTrigger> triggerFactory);

    // Events
    <T extends GameEvent> void addEventListener(Function<ServerRuntime, EventRegistry<T>> registry);

    // Templates
    <T, K> void addCustomTemplate(String templateName, Class<T> templateClass, Function<T, K> keyExtractor, Path file);

    // ===== Storage =====
    void mobileStorage(MobileStorage mobileStorage);

    void itemStorage(ItemStorage itemStorage);

    void accountStorage(AccountStorage accountStorage);

    // ===== Read methods (optional, for engine use) =====
    Function<World, Wallet> wallet();

    Supplier<PricingStrategy> pricingStrategy();

    List<AI> aiList();

    List<BehaviorProfile> behaviorProfileList();

    List<Function<ServerRuntime, ItemUseTrigger>> itemTriggerList();

    List<Function<ServerRuntime, EventRegistry<GameEvent>>> eventListenerList();

    <K, V> List<DefaultWorldCfg.TemplateData<K, V>> templateList();

    MobileStorage mobileStorage();

    ItemStorage itemStorage();

    AccountStorage accountStorage();

}
