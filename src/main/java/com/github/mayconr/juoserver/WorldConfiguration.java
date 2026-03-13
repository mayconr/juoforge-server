package com.github.mayconr.juoserver;

import com.github.mayconr.juoserver.game.ai.AI;
import com.github.mayconr.juoserver.game.ai.BehaviorProfile;
import com.github.mayconr.juoserver.game.ai.ais.DialogueReactiveAI;
import com.github.mayconr.juoserver.game.ai.ais.PassiveAnimalAI;
import com.github.mayconr.juoserver.game.ai.profiles.AnimalBehaviorProfile;
import com.github.mayconr.juoserver.game.ai.profiles.BankerBehaviorProfile;
import com.github.mayconr.juoserver.game.ai.profiles.VendorBehaviorProfile;
import com.github.mayconr.juoserver.game.economy.PricingStrategy;
import com.github.mayconr.juoserver.game.economy.ScarcityBasedPricingStrategy;
import com.github.mayconr.juoserver.game.wallet.PhisycalGoldWallet;
import com.github.mayconr.juoserver.game.wallet.Wallet;
import com.github.mayconr.juoserver.game.world.World;
import com.github.mayconr.juoserver.infrastructure.storage.AccountStorage;
import com.github.mayconr.juoserver.infrastructure.storage.ItemStorage;
import com.github.mayconr.juoserver.infrastructure.storage.MobileStorage;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public record WorldConfiguration(Function<World, Wallet> wallet, Supplier<PricingStrategy> pricingStrategy,
                                 List<AI> aiList, List<BehaviorProfile> behaviorProfileList, MobileStorage mobileStorage,
                                 ItemStorage itemStorage, AccountStorage accountStorage) {

    public static ShardConfigurationBuilder builder() {
        return new ShardConfigurationBuilder();
    }

    public static class ShardConfigurationBuilder {
        // ===== Economy =====
        private Function<World, Wallet> wallet = PhisycalGoldWallet::new;
        private Supplier<PricingStrategy> pricingStrategy = ScarcityBasedPricingStrategy::new;

        // ===== AI =====
        private final List<AI> aiList = new ArrayList<>(defaultAIs());
        private List<AI> defaultAIs() {
            return List.of(new DialogueReactiveAI(), new PassiveAnimalAI());
        }

        private final List<BehaviorProfile> behaviorProfileList = new ArrayList<>(defaultBehaviorProfiles());
        private List<BehaviorProfile> defaultBehaviorProfiles() {
            return List.of(new VendorBehaviorProfile(), new BankerBehaviorProfile(), new AnimalBehaviorProfile());
        }

        // ===== Storage =====
        private MobileStorage mobileStorage;
        private ItemStorage itemStorage;
        private AccountStorage accountStorage;

        // ===== Sub Builders =====
        public ShardConfigurationBuilder economy(Consumer<EconomySubBuilder> consumer) {
            EconomySubBuilder sub = new EconomySubBuilder(this);
            consumer.accept(sub);
            return this;
        }

        public ShardConfigurationBuilder ai(Consumer<AISubBuilder> consumer) {
            AISubBuilder sub = new AISubBuilder(this);
            consumer.accept(sub);
            return this;
        }

        public ShardConfigurationBuilder storage(Consumer<StorageBuilder> consumer) {
            StorageBuilder sub = new StorageBuilder(this);
            consumer.accept(sub);
            return this;
        }

        // ===== Build Override (immutability) =====

        public WorldConfiguration build() {
            return new WorldConfiguration(
                    wallet,
                    pricingStrategy,
                    List.copyOf(aiList),
                    List.copyOf(behaviorProfileList),
                    mobileStorage,
                    itemStorage,
                    accountStorage
            );
        }
    }

    // =========================
    // Economy Sub Builder
    // =========================

    @RequiredArgsConstructor
    public static class EconomySubBuilder {

        private final ShardConfigurationBuilder parent;

        public EconomySubBuilder wallet(Function<World, Wallet> walletFactory) {
            parent.wallet = walletFactory;
            return this;
        }

        public EconomySubBuilder pricingStrategy(Supplier<PricingStrategy> pricingStrategyFactory) {
            parent.pricingStrategy = pricingStrategyFactory;
            return this;
        }

    }

    // =========================
    // AI Sub Builder
    // =========================

    @RequiredArgsConstructor
    public static class AISubBuilder {

        private final ShardConfigurationBuilder parent;

        public AISubBuilder addNpcAI(AI ai) {
            parent.aiList.add(ai);
            return this;
        }

        public AISubBuilder addBehaviorProfile(BehaviorProfile profile) {
            parent.behaviorProfileList.add(profile);
            return this;
        }
    }

    @RequiredArgsConstructor
    public static class StorageBuilder {
        private final ShardConfigurationBuilder parent;

        public StorageBuilder mobile(MobileStorage mobileStorage) {
            parent.mobileStorage = mobileStorage;
            return this;
        }

        public StorageBuilder item(ItemStorage itemStorage) {
            parent.itemStorage = itemStorage;
            return this;
        }

        public StorageBuilder account(AccountStorage accountStorage) {
            parent.accountStorage = accountStorage;
            return this;
        }
    }
}
