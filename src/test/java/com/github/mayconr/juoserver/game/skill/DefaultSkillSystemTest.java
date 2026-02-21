package com.github.mayconr.juoserver.game.skill;

import com.github.mayconr.juoserver.ServerProperties;
import com.github.mayconr.juoserver.game.TestFactory;
import com.github.mayconr.juoserver.game.model.SkillContainer;
import com.github.mayconr.juoserver.game.model.SkillValue;
import com.github.mayconr.juoserver.game.world.WorldInternal;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.rng.AlwaysFailRNG;
import com.github.mayconr.juoserver.infrastructure.rng.AlwaysSuccessRNG;
import com.github.mayconr.juoserver.infrastructure.rng.SeededRNG;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class DefaultSkillSystemTest {

    private static final int TEST_SKILL_ID = 0;

    private ServerProperties properties;
    @Mock
    private WorldInternal worldInternal;
    @Mock
    private EventBus eventBus;

    @BeforeEach
    void setUp() {
        var skillsConfig = new ServerProperties.Skills(
                0.0,   // minGainChance
                0.5,   // maxGainChance
                50,
                100// balanceOffset
        );
        properties = new ServerProperties(null, null, skillsConfig, null, null, null);
    }

    @Test
    void shouldNotGainWhenRngFails() {
        var system = new DefaultSkillSystem(properties, new AlwaysFailRNG(), eventBus);

        var mobile = TestFactory.createTestMobile();
        var mining = SkillValue.of(TEST_SKILL_ID, 0, 100);
        mobile.setSkills(new SkillContainer(List.of(mining)));

        system.tryGain(mobile, TEST_SKILL_ID, 50, null);

        assertEquals(0.0, mining.getBase(), 0.0001);
    }

    @Test
    void shouldGainSkillWhenRngSucceeds() {
        var system = new DefaultSkillSystem(properties, new AlwaysSuccessRNG(), eventBus);

        var mobile = TestFactory.createTestMobile();
        var mining = SkillValue.of(TEST_SKILL_ID, 0, 100);
        mobile.setSkills(new SkillContainer(List.of(mining)));

        system.tryGain(mobile, TEST_SKILL_ID, 50, null);

        assertEquals(0.05, mining.getBase(), 0.0001);
    }

    @Test
    void shouldReduceGainAsSkillApproachesCap() {
        var system = new DefaultSkillSystem(properties, new AlwaysSuccessRNG(), eventBus);

        var mobile = TestFactory.createTestMobile();
        var mining = SkillValue.of(TEST_SKILL_ID, 40, 50);
        mobile.setSkills(new SkillContainer(List.of(mining)));

        system.tryGain(mobile, TEST_SKILL_ID, 50, null);

        // factor = 1 - (40/50) = 0.2
        // gain = 0.1 * 0.2 = 0.02
        assertEquals(40.02, mining.getBase(), 0.0001);
    }

    @Test
    void shouldClampGainNearCap() {
        var system = new DefaultSkillSystem(properties, new AlwaysSuccessRNG(), eventBus);

        var mobile = TestFactory.createTestMobile();
        var mining = SkillValue.of(TEST_SKILL_ID, 49.9, 50);
        mobile.setSkills(new SkillContainer(List.of(mining)));

        system.tryGain(mobile, TEST_SKILL_ID, 50, null);

        // factor clamped for 0.1
        // gain = 0.01
        assertEquals(49.91, mining.getBase(), 0.0001);
    }

    @Test
    void shouldNeverExceedSkillCap() {
        var system = new DefaultSkillSystem(properties, new AlwaysSuccessRNG(), eventBus);

        var mobile = TestFactory.createTestMobile();
        var mining = SkillValue.of(TEST_SKILL_ID, 49.9, 50);
        mobile.setSkills(new SkillContainer(List.of(mining)));

        system.tryGain(mobile, TEST_SKILL_ID, 50, null);

        assertTrue(mining.getBase() <= 50.0);
    }

    @Test
    void shouldClampExactlyAtCapWhenOverflowing() {
        var system = new DefaultSkillSystem(properties, new AlwaysSuccessRNG(),eventBus);

        var mobile = TestFactory.createTestMobile();
        var mining = SkillValue.of(TEST_SKILL_ID, 49.99, 50);
        mobile.setSkills(new SkillContainer(List.of(mining)));

        system.tryGain(mobile, TEST_SKILL_ID, 50, null);
        system.tryGain(mobile, TEST_SKILL_ID, 50, null);

        assertEquals(50.0, mining.getBase(), 0.0001);
    }

    @Test
    void shouldSimulateMultipleSwingsAndApproachCap() {
        var system = new DefaultSkillSystem(properties, new AlwaysSuccessRNG(), eventBus);

        var skill = SkillValue.of(TEST_SKILL_ID, 0, 50);
        var mobile = TestFactory.createTestMobile(List.of(skill));

        double lastValue = skill.getBase();

        int swings = 3000;

        for (int i = 0; i < swings; i++) {
            system.tryGain(mobile, skill.getSkillId(), 50, null);

            double current = skill.getBase();

            // never reduce
            assertTrue(current >= lastValue,
                    "Skill should never decrease");

            // never pass the cap
            assertTrue(current <= skill.getCap(),
                    "Skill should never exceed cap");

            lastValue = current;
        }

        // after manu swing, should me close to the cap
        assertTrue(skill.getBase() > 45.0,
                "Skill should approach cap after many swings");
    }

    @Test
    void veryLowDifficultyShouldProgressSlowerThanHighDifficulty() {
        int swings = 3000;

        // low difficult
        var lowRng = new SeededRNG(123L);
        var lowSystem = new DefaultSkillSystem(properties, lowRng, eventBus);

        var lowSkill = SkillValue.of(TEST_SKILL_ID, 0, 50);
        var lowMobile = TestFactory.createTestMobile(List.of(lowSkill));

        for (int i = 0; i < swings; i++) {
            lowSystem.tryGain(lowMobile, TEST_SKILL_ID, 0, null);
        }

        // high difficult
        var highRng = new SeededRNG(123L);
        var highSystem = new DefaultSkillSystem(properties, highRng, eventBus);

        var highSkill = SkillValue.of(TEST_SKILL_ID, 0, 50);
        var highMobile = TestFactory.createTestMobile(List.of(highSkill));

        for (int i = 0; i < swings; i++) {
            highSystem.tryGain(highMobile, TEST_SKILL_ID, 100, null);
        }

        assertTrue(lowSkill.getBase() < highSkill.getBase(),
                "Low difficulty should progress slower than high difficulty. ["+lowSkill.getBase()+" | "+highSkill.getBase()+"]");
    }

    @Test
    void shouldGainFromZeroToGM() {
        var skillsProps = new ServerProperties.Skills(
                0.0,   // minGainChance
                1.0,   // maxGainChance
                50, // balanceOffset
                100
        );

        var properties = new ServerProperties(null, null, skillsProps, null, null, null);

        var system = new DefaultSkillSystem(properties, new AlwaysSuccessRNG(), eventBus);

        // skill 0 → 100 (GM)
        var skill = SkillValue.of(TEST_SKILL_ID, 0.0, 100.0);
        var mobile = TestFactory.createTestMobile(List.of(skill));

        double lastValue = skill.getBase();

        int swings = 10_000;

        for (int i = 0; i < swings; i++) {
            system.tryGain(mobile, TEST_SKILL_ID, 100, null);

            double current = skill.getBase();

            // never reduced
            assertTrue(current >= lastValue,
                    "Skill should never decrease");

            // never after cap
            assertTrue(current <= skill.getCap(),
                    "Skill should never exceed cap");

            lastValue = current;

            // reached GM
            if (current >= skill.getCap()) {
                break;
            }
        }

        assertEquals(
                skill.getCap(),
                skill.getBase(),
                0.0001,
                "Skill should reach GM exactly"
        );
    }
}