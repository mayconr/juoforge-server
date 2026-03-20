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
        Economy economy
) {
    public record Mobile(String backpackItem) {}
    public record Vitals(int saturationFactor) {}
    public record GameLoop(int tps) {}
    public record Skills(double minGainChance, double maxGainChance, int balanceOffset, double cap) {}
    public record World(int lightOfSight) {}
    public record Economy(String goldCoinItem) {}
    public record Files(String dataFileRoot) {}
    public record Client(List<ClientFeatureFlag> unlockedFeatures) {}
}
