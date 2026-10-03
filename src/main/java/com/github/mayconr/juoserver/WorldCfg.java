package com.github.mayconr.juoserver;

import com.github.mayconr.juoserver.game.economy.PricingStrategy;
import com.github.mayconr.juoserver.game.combat.progression.CombatSkillGainPolicy;
import com.github.mayconr.juoserver.game.skill.SkillSystemFactory;
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

    void content(WorldContent content);

    WorldContent content();

    /** AI timing loaded from the server configuration. */
    default com.github.mayconr.juoserver.game.GamePlaySettings.Ai ai() {
        return content().settings().ai();
    }

    /** Registers a shard flow factory. Context types must be unique across core and shard flows. */
    <T extends AbstractContext> void addFlow(
            Class<T> contextType,
            Function<ServerRuntime, Flow<T>> factory);

    List<ShardFlowRegistration<?>> flowList();

    /** Replaces the default weapon-skill and Tactics gain policy for combat swings. */
    void combatSkillGainPolicy(Supplier<CombatSkillGainPolicy> factory);

    Supplier<CombatSkillGainPolicy> combatSkillGainPolicy();

    /** Replaces the skill system used for all skill gain attempts in this world. */
    void skillSystem(SkillSystemFactory factory);

    SkillSystemFactory skillSystem();

    // ===== Economy =====
    void wallet(Function<World, Wallet> walletFactory);

    void pricingStrategy(Supplier<PricingStrategy> pricingStrategyFactory);

    // Item use trigger
    void addItemTrigger(Function<ServerRuntime, ItemUseTrigger> triggerFactory);

    /** Registers a spell trigger factory. The first supporting trigger handles each request. */
    void addSpellTrigger(Function<ServerRuntime, SpellCastTrigger> triggerFactory);

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

    List<Function<ServerRuntime, ItemUseTrigger>> itemTriggerList();

    List<Function<ServerRuntime, SpellCastTrigger>> spellTriggerList();

    List<Function<ServerRuntime, EventRegistry<GameEvent>>> eventListenerList();

    <K, V> List<DefaultWorldCfg.TemplateData<K, V>> templateList();

    MobileStorage mobileStorage();

    ItemStorage itemStorage();

    AccountStorage accountStorage();

}
