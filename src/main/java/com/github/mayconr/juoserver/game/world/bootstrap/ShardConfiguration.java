package com.github.mayconr.juoserver.game.world.bootstrap;

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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public record ShardConfiguration(Function<World, Wallet> wallet, Supplier<PricingStrategy> pricingStrategy,
                                 List<AI> aiList, List<BehaviorProfile> behaviorProfileList) {

    public static ShardConfigurationBuilder builder() {
        return new ShardConfigurationBuilder();
    }

    public static class ShardConfigurationBuilder {
        // ===== Defaults =====
        private Function<World, Wallet> wallet = PhisycalGoldWallet::new;
        private Supplier<PricingStrategy> pricingStrategy = ScarcityBasedPricingStrategy::new;

        private List<AI> aiList = new ArrayList<>(defaultAIs());
        private List<AI> defaultAIs() {
            return List.of(new DialogueReactiveAI(), new PassiveAnimalAI());
        }

        private List<BehaviorProfile> behaviorProfileList = new ArrayList<>(defaultBehaviorProfiles());
        private List<BehaviorProfile> defaultBehaviorProfiles() {
            return List.of(new VendorBehaviorProfile(), new BankerBehaviorProfile(), new AnimalBehaviorProfile());
        }

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

        // ===== Build Override (immutability) =====

        public ShardConfiguration build() {
            return new ShardConfiguration(
                    wallet,
                    pricingStrategy,
                    List.copyOf(aiList),
                    List.copyOf(behaviorProfileList)
            );
        }
    }

    // =========================
    // Economy Sub Builder
    // =========================

    public static class EconomySubBuilder {

        private final ShardConfigurationBuilder parent;

        private EconomySubBuilder(ShardConfigurationBuilder parent) {
            this.parent = parent;
        }

        public EconomySubBuilder wallet(Function<World, Wallet> wallet) {
            parent.wallet = wallet;
            return this;
        }

        public EconomySubBuilder pricingStrategy(Supplier<PricingStrategy> strategy) {
            parent.pricingStrategy = strategy;
            return this;
        }

    }

    // =========================
    // AI Sub Builder
    // =========================

    public static class AISubBuilder {

        private final ShardConfigurationBuilder parent;

        private AISubBuilder(ShardConfigurationBuilder parent) {
            this.parent = parent;
        }

        public AISubBuilder addNpcAI(AI ai) {
            parent.aiList.add(ai);
            return this;
        }

        public AISubBuilder addBehaviorProfile(BehaviorProfile profile) {
            parent.behaviorProfileList.add(profile);
            return this;
        }
    }
}
