package com.github.mayconr.juoserver.game;

import com.github.mayconr.juoserver.network.packet.EnableLockedClientFeatures.ClientFeatureFlag;

import java.util.List;

public record GamePlaySettings(
        String name,
        Vitals vitals,
        GameLoop gameLoop,
        Skills skills,
        Mobile mobile,
        World world,
        Files files,
        Client client,
        Economy economy,
        Ai ai
) {
    public record Mobile(String backpackItem) {}
    public record Vitals(int saturationFactor) {}
    public record GameLoop(int tps) {}
    public record Ai(double updateIntervalSeconds) {
        public Ai {
            if (!Double.isFinite(updateIntervalSeconds) || updateIntervalSeconds <= 0) {
                throw new IllegalArgumentException("ai.updateIntervalSeconds must be finite and positive");
            }
        }
    }
    public record Skills(
            double minGainChance,
            double maxGainChance,
            int balanceOffset,
            double cap,
            double beginnerGainMultiplier,
            double beginnerGainThreshold
    ) {}

    public record World(Visibility visibility, Interaction interaction) {}
    public record Visibility(int range) {}
    public record Interaction(MoveItem moveItem) {}
    public record MoveItem(int maxDistance) {}

    public record Economy(String goldCoinItem) {}
    public record Files(String dataFileRoot) {}
    public record Client(List<ClientFeatureFlag> unlockedFeatures) {}
}
