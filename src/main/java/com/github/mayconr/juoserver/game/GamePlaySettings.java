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
        Ui ui,
        Temporal temporal,
        Items items
) {
    public GamePlaySettings {
        ui = ui == null ? new Ui() : ui;
        temporal = temporal == null ? new Temporal() : temporal;
        items = items == null ? new Items() : items;
    }

    /** Retains the constructor used before item settings were introduced. */
    public GamePlaySettings(String name, GameLoop gameLoop, Skills skills, Mobile mobile, World world,
                            Files files, Client client, Economy economy, Ai ai, Ui ui, Temporal temporal) {
        this(name, gameLoop, skills, mobile, world, files, client, economy, ai, ui, temporal, new Items());
    }

    /** Retains the constructor used before temporal settings were introduced. */
    public GamePlaySettings(String name, GameLoop gameLoop, Skills skills, Mobile mobile, World world,
                            Files files, Client client, Economy economy, Ai ai, Ui ui) {
        this(name, gameLoop, skills, mobile, world, files, client, economy, ai, ui, new Temporal());
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

    /**
     * Conversion between real time and game time.
     *
     * @param realSecondsPerGameMinute real seconds needed to advance one game minute
     */
    public record Temporal(double realSecondsPerGameMinute) {
        public static final double DEFAULT_REAL_SECONDS_PER_GAME_MINUTE = 5.0;

        public Temporal() {
            this(DEFAULT_REAL_SECONDS_PER_GAME_MINUTE);
        }

        public Temporal {
            if (!Double.isFinite(realSecondsPerGameMinute) || realSecondsPerGameMinute <= 0) {
                throw new IllegalArgumentException("temporal.realSecondsPerGameMinute must be finite and positive");
            }
        }
    }

    public GamePlaySettings withUi(Ui ui) {
        Objects.requireNonNull(ui, "ui is required");
        return new GamePlaySettings(
                name, gameLoop, skills, mobile, world, files, client, economy, ai, ui, temporal, items
        );
    }

    public GamePlaySettings withTemporal(Temporal temporal) {
        Objects.requireNonNull(temporal, "temporal is required");
        return new GamePlaySettings(
                name, gameLoop, skills, mobile, world, files, client, economy, ai, ui, temporal, items
        );
    }

    public GamePlaySettings withItems(Items items) {
        Objects.requireNonNull(items, "items is required");
        return new GamePlaySettings(
                name, gameLoop, skills, mobile, world, files, client, economy, ai, ui, temporal, items
        );
    }

    /** Item rules. */
    public record Items(ItemQuality quality, ItemDecay decay) {
        public Items() {
            this(new ItemQuality(), new ItemDecay());
        }

        public Items(ItemQuality quality) {
            this(quality, new ItemDecay());
        }

        public Items {
            quality = quality == null ? new ItemQuality() : quality;
            decay = decay == null ? new ItemDecay() : decay;
        }
    }

    /** Real seconds of server activity; stopped-server time never advances decay. */
    public record ItemDecay(double durationSeconds, double scanIntervalSeconds) {
        public ItemDecay() {
            this(120.0, 60.0);
        }

        public ItemDecay {
            validateSeconds(durationSeconds, "items.decay.durationSeconds");
            validateSeconds(scanIntervalSeconds, "items.decay.scanIntervalSeconds");
        }

        private static void validateSeconds(double value, String name) {
            if (!Double.isFinite(value) || value <= 0 || value >= Long.MAX_VALUE / 1000.0) {
                throw new IllegalArgumentException(name + " must be finite, positive and fit in milliseconds");
            }
        }
    }

    /**
     * Ranges used to roll item quality. Values outside a range are clamped to its nearest bound.
     *
     * @param minModifier modifier that guarantees minimum quality
     * @param maxModifier modifier that guarantees maximum quality
     * @param minLuck luck with the strongest bias toward lower quality; must not be positive
     * @param maxLuck luck with the strongest bias toward higher quality; must not be negative
     */
    public record ItemQuality(double minModifier, double maxModifier, int minLuck, int maxLuck) {
        public static final double DEFAULT_MIN_MODIFIER = 0.0;
        public static final double DEFAULT_MAX_MODIFIER = 100.0;
        public static final int DEFAULT_MIN_LUCK = -100;
        public static final int DEFAULT_MAX_LUCK = 100;

        public ItemQuality() {
            this(DEFAULT_MIN_MODIFIER, DEFAULT_MAX_MODIFIER, DEFAULT_MIN_LUCK, DEFAULT_MAX_LUCK);
        }

        public ItemQuality {
            if (!Double.isFinite(minModifier) || !Double.isFinite(maxModifier) || minModifier >= maxModifier) {
                throw new IllegalArgumentException(
                        "items.quality modifier range must be finite and minModifier must be less than maxModifier");
            }
            if (minLuck > 0 || maxLuck < 0) {
                throw new IllegalArgumentException("items.quality luck range must contain zero");
            }
        }
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
        return new GamePlaySettings(
                name, gameLoop, skills, mobile, world, files, client, economy, ai, ui, temporal, items
        );
    }

    public GamePlaySettings withSkills(Skills skills) {
        Objects.requireNonNull(skills, "skills is required");
        return new GamePlaySettings(
                name, gameLoop, skills, mobile, world, files, client, economy, ai, ui, temporal, items
        );
    }

    public GamePlaySettings withMobile(Mobile mobile) {
        Objects.requireNonNull(mobile, "mobile is required");
        return new GamePlaySettings(
                name, gameLoop, skills, mobile, world, files, client, economy, ai, ui, temporal, items
        );
    }

    public GamePlaySettings withWorld(World world) {
        Objects.requireNonNull(world, "world is required");
        return new GamePlaySettings(
                name, gameLoop, skills, mobile, world, files, client, economy, ai, ui, temporal, items
        );
    }

    public GamePlaySettings withClient(Client client) {
        Objects.requireNonNull(client, "client is required");
        return new GamePlaySettings(
                name, gameLoop, skills, mobile, world, files, client, economy, ai, ui, temporal, items
        );
    }

    public GamePlaySettings withEconomy(Economy economy) {
        Objects.requireNonNull(economy, "economy is required");
        return new GamePlaySettings(
                name, gameLoop, skills, mobile, world, files, client, economy, ai, ui, temporal, items
        );
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
