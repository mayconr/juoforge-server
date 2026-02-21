package com.github.mayconr.juoserver;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "server")
public record ServerProperties(Vitals vitals, GameLoop gameLoop, Skills skills,
                               Mobile mobile, World world, Files files) {

    @ConstructorBinding
    public ServerProperties {
    }

    public record Mobile(String backpack) {
        @ConstructorBinding
        public Mobile(@DefaultValue("backpack") String backpack) {
            this.backpack = backpack;
        }
    }

    /**
     * @param saturationFactor Controls how quickly regen approaches its maximum as the stat increases.
     */
    public record Vitals(int saturationFactor) {
        @ConstructorBinding
        public Vitals(@DefaultValue("100") int saturationFactor) {
            this.saturationFactor = saturationFactor;
        }
    }

    public record GameLoop(int tps) {
        @ConstructorBinding
        public GameLoop(@DefaultValue("20")int tps) {
            this.tps = tps;
        }
    }

    /**
     * Configuration values used by the skill system to calculate the probability
     * of gaining skill points after performing an action.
     *
     * <p>This configuration is generic and system-wide. It does not contain
     * skill-specific rules or gameplay logic, but instead defines the bounds
     * and balance parameters used by the skill gain formula.</p>
     *
     * <h3>Skill gain chance formula</h3>
     *
     * <pre>
     * chance = clamp(
     *     (difficulty - skillValue + balanceOffset) / 100,
     *     minGainChance,
     *     maxGainChance
     * );
     * </pre>
     *
     * <h3>Parameters</h3>
     *
     * <ul>
     *   <li>
     *     <b>minGainChance</b> – The minimum probability (0.0 to 1.0) of gaining
     *     skill from an action. This prevents progression from becoming
     *     completely blocked at high skill values or low-difficulty actions.
     *   </li>
     *   <li>
     *     <b>maxGainChance</b> – The maximum probability (0.0 to 1.0) of gaining
     *     skill from a single action. This prevents guaranteed skill gains and
     *     helps avoid excessive or automated progression.
     *   </li>
     *   <li>
     *     <b>balanceOffset</b> – A balancing offset applied to the formula that
     *     defines the equilibrium point where the chance of gaining skill is
     *     approximately 50%. Higher values make skill gains easier overall,
     *     while lower values make progression slower and more demanding.
     *   </li>
     * </ul>
     *
     * <p>The default values are chosen to approximate a classic Ultima
     * Online–style progression curve, where skill gains are frequent at lower
     * levels and become increasingly rare as the skill approaches its upper
     * range.</p>
     */
    public record Skills(double minGainChance, double maxGainChance, int balanceOffset, double cap) {
        @ConstructorBinding
        public Skills(@DefaultValue("0") double minGainChance, @DefaultValue("0.50") double maxGainChance, @DefaultValue("50") int balanceOffset, @DefaultValue("50") double cap) {
            this.minGainChance = minGainChance;
            this.maxGainChance = maxGainChance;
            this.balanceOffset = balanceOffset;
            this.cap = cap;
        }
    }


    public record World(int lightOfSight) {
        @ConstructorBinding
        public World(@DefaultValue("24") int lightOfSight) {
            this.lightOfSight = lightOfSight;
        }
    }

    public record Files(String dataFileRoot) {
        @ConstructorBinding
        public Files(String dataFileRoot) {
            this.dataFileRoot = dataFileRoot;
        }
    }

}
