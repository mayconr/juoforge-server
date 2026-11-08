package com.github.mayconr.juoserver.game;

import com.github.mayconr.juoserver.network.packet.EnableLockedClientFeatures.ClientFeatureFlag;

import java.util.List;
import java.util.Objects;

public record GamePlaySettings(
        String name,
        GameLoop gameLoop,
        Skills skills,
        Mobile mobile,
        World world,
        Files files,
        Client client,
        Economy economy,
        Ai ai,
        Ui ui
) {
    public GamePlaySettings {
        ui = ui == null ? new Ui() : ui;
    }

    /** Retains the constructor used before UI settings were introduced. */
    public GamePlaySettings(String name, GameLoop gameLoop, Skills skills, Mobile mobile,
                            World world, Files files, Client client, Economy economy, Ai ai) {
        this(name, gameLoop, skills, mobile, world, files, client, economy, ai, new Ui());
    }

    public record Ui(double defaultGumpTimeoutSeconds) {
        public Ui() { this(30.0); }

        public Ui {
            if (!Double.isFinite(defaultGumpTimeoutSeconds) || defaultGumpTimeoutSeconds <= 0
                    || defaultGumpTimeoutSeconds >= Long.MAX_VALUE / 1000.0) {
                throw new IllegalArgumentException("ui.defaultGumpTimeoutSeconds must be finite, positive and fit in milliseconds");
            }
        }
    }

    public GamePlaySettings withUi(Ui ui) {
        return new GamePlaySettings(name, gameLoop, skills, mobile, world, files, client, economy, ai,
                Objects.requireNonNull(ui, "ui is required"));
    }

    /** Default gameplay rules; server timing and data files are supplied by WorldConfigurator. */
    public GamePlaySettings() {
        this("DEFAULT", null,
                new Skills(0.0, 0.5, 50, 100, 100, 30.0, 0.00113654),
                new Mobile("backpack", 2), new World(new Visibility(24), null),
                null, new Client(List.of(ClientFeatureFlag.AOS)), new Economy("gold_coin"), null);
    }

    public GamePlaySettings withName(String name) {
        Objects.requireNonNull(name, "name is required");
        return new GamePlaySettings(name, this.gameLoop, this.skills, this.mobile, this.world, this.files, this.client, this.economy, this.ai, this.ui);
    }

    public GamePlaySettings withSkills(Skills skills) {
        Objects.requireNonNull(skills, "skills is required");
        return new GamePlaySettings(this.name, this.gameLoop, skills, this.mobile, this.world, this.files, this.client, this.economy, this.ai, this.ui);
    }

    public GamePlaySettings withMobile(Mobile mobile) {
        Objects.requireNonNull(mobile, "mobile is required");
        return new GamePlaySettings(this.name, this.gameLoop, this.skills, mobile, this.world, this.files, this.client, this.economy, this.ai, this.ui);
    }

    public GamePlaySettings withWorld(World world) {
        Objects.requireNonNull(world, "world is required");
        return new GamePlaySettings(this.name, this.gameLoop, this.skills, this.mobile, world, this.files, this.client, this.economy, this.ai, this.ui);
    }

    public GamePlaySettings withClient(Client client) {
        Objects.requireNonNull(client, "client is required");
        return new GamePlaySettings(this.name, this.gameLoop, this.skills, this.mobile, this.world, this.files, client, this.economy, this.ai, this.ui);
    }

    public GamePlaySettings withEconomy(Economy economy) {
        Objects.requireNonNull(economy, "economy is required");
        return new GamePlaySettings(this.name, this.gameLoop, this.skills, this.mobile, this.world, this.files, this.client, economy, this.ai, this.ui);
    }

    public record Mobile(String backpackItem, Integer mountRange, CarryWeight carryWeight, Vitals vitals) {
        public static final int DEFAULT_MOUNT_RANGE = 2;

        public Mobile(String backpackItem, Integer mountRange) {
            this(backpackItem, mountRange, null, null);
        }

        public Mobile(String backpackItem, Integer mountRange, CarryWeight carryWeight) {
            this(backpackItem, mountRange, carryWeight, null);
        }

        public Mobile withVitals(Vitals vitals) {
            Objects.requireNonNull(vitals, "vitals is required");
            return new Mobile(backpackItem, mountRange, carryWeight, vitals);
        }

        public Mobile {
            vitals = vitals == null ? new Vitals(100) : vitals;
            mountRange = mountRange == null ? DEFAULT_MOUNT_RANGE : mountRange;
            carryWeight = carryWeight == null ? new CarryWeight() : carryWeight;
            if (mountRange < 0) {
                throw new IllegalArgumentException("mobile.mountRange must not be negative");
            }
        }
    }
    public record CarryWeight(Integer baseCapacity, Double capacityPerStrength) {
        public static final int DEFAULT_BASE_CAPACITY = 20;
        public static final double DEFAULT_CAPACITY_PER_STRENGTH = 0.6;

        public CarryWeight() {
            this(DEFAULT_BASE_CAPACITY, DEFAULT_CAPACITY_PER_STRENGTH);
        }

        public CarryWeight {
            baseCapacity = baseCapacity == null ? DEFAULT_BASE_CAPACITY : baseCapacity;
            capacityPerStrength = capacityPerStrength == null
                    ? DEFAULT_CAPACITY_PER_STRENGTH : capacityPerStrength;
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
            double beginnerGainThreshold,
            double baseGain
    ) {
        public Skills {
            if (!Double.isFinite(baseGain) || baseGain <= 0) {
                throw new IllegalArgumentException("skills.baseGain must be finite and positive");
            }
        }
    }

    public record World(Visibility visibility, Interaction interaction) {}
    public record Visibility(int range) {}
    public record Interaction(MoveItem moveItem) {}
    public record MoveItem(int maxDistance) {}

    public record Economy(String goldCoinItem) {}
    public record Files(String dataFileRoot) {}
    public record Client(List<ClientFeatureFlag> unlockedFeatures) {}
}
