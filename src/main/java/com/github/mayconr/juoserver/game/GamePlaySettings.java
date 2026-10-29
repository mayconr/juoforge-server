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
    /** Default gameplay rules; server timing and data files are supplied by WorldCfg. */
    public GamePlaySettings() {
        this("DEFAULT", new Vitals(100), null,
                new Skills(0.0, 0.5, 50, 50, 3.0, 30.0),
                new Mobile("backpack", 2), new World(new Visibility(24), null),
                null, new Client(List.of(ClientFeatureFlag.AOS)), new Economy("gold_coin"), null);
    }

    public GamePlaySettings withName(String name) {
        java.util.Objects.requireNonNull(name, "name is required");
        return new GamePlaySettings(name, this.vitals, this.gameLoop, this.skills, this.mobile, this.world, this.files, this.client, this.economy, this.ai);
    }

    public GamePlaySettings withVitals(Vitals vitals) {
        java.util.Objects.requireNonNull(vitals, "vitals is required");
        return new GamePlaySettings(this.name, vitals, this.gameLoop, this.skills, this.mobile, this.world, this.files, this.client, this.economy, this.ai);
    }

    public GamePlaySettings withSkills(Skills skills) {
        java.util.Objects.requireNonNull(skills, "skills is required");
        return new GamePlaySettings(this.name, this.vitals, this.gameLoop, skills, this.mobile, this.world, this.files, this.client, this.economy, this.ai);
    }

    public GamePlaySettings withMobile(Mobile mobile) {
        java.util.Objects.requireNonNull(mobile, "mobile is required");
        return new GamePlaySettings(this.name, this.vitals, this.gameLoop, this.skills, mobile, this.world, this.files, this.client, this.economy, this.ai);
    }

    public GamePlaySettings withWorld(World world) {
        java.util.Objects.requireNonNull(world, "world is required");
        return new GamePlaySettings(this.name, this.vitals, this.gameLoop, this.skills, this.mobile, world, this.files, this.client, this.economy, this.ai);
    }

    public GamePlaySettings withClient(Client client) {
        java.util.Objects.requireNonNull(client, "client is required");
        return new GamePlaySettings(this.name, this.vitals, this.gameLoop, this.skills, this.mobile, this.world, this.files, client, this.economy, this.ai);
    }

    public GamePlaySettings withEconomy(Economy economy) {
        java.util.Objects.requireNonNull(economy, "economy is required");
        return new GamePlaySettings(this.name, this.vitals, this.gameLoop, this.skills, this.mobile, this.world, this.files, this.client, economy, this.ai);
    }

    public record Mobile(String backpackItem, Integer mountRange) {
        public static final int DEFAULT_MOUNT_RANGE = 2;

        public Mobile {
            mountRange = mountRange == null ? DEFAULT_MOUNT_RANGE : mountRange;
            if (mountRange < 0) {
                throw new IllegalArgumentException("mobile.mountRange must not be negative");
            }
        }
    }
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
