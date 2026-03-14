package com.github.mayconr.juoserver;

import com.github.mayconr.juoserver.network.packet.EnableLockedClientFeatures;

public record EngineSettings(
        Vitals vitals,
        GameLoop gameLoop,
        Skills skills,
        Mobile mobile,
        World world,
        Files files,
        Client client,
        Economy economy
) {
    public static EngineSettings defaults() {
        return new EngineSettings(
                new Vitals(100),
                new GameLoop(20),
                new Skills(0.0, 0.50, 50, 50.0),
                new Mobile("backpack"),
                new World(24),
                new Files("C:\\Program Files (x86)\\Electronic Arts\\Ultima Online Classic"),
                new Client(EnableLockedClientFeatures.ClientFeatureFlags.T2A
                        | EnableLockedClientFeatures.ClientFeatureFlags.RENAISSANCE
                        | EnableLockedClientFeatures.ClientFeatureFlags.LBR
                        | EnableLockedClientFeatures.ClientFeatureFlags.AOS
                        | EnableLockedClientFeatures.ClientFeatureFlags.SE
                        | EnableLockedClientFeatures.ClientFeatureFlags.ML
                        | EnableLockedClientFeatures.ClientFeatureFlags.KR_FACES),
                new Economy("gold_coin")
        );
    }

    public record Mobile(String backpack) {}
    public record Vitals(int saturationFactor) {}
    public record GameLoop(int tps) {}
    public record Skills(double minGainChance, double maxGainChance, int balanceOffset, double cap) {}
    public record World(int lightOfSight) {}
    public record Economy(String goldCoinItem) {}
    public record Files(String dataFileRoot) {}
    public record Client(int unlockedFeatures) {}
}
