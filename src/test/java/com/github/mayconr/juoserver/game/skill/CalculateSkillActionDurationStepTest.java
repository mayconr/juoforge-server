package com.github.mayconr.juoserver.game.skill;

import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.skill.flow.start.CalculateSkillActionDurationStep;
import com.github.mayconr.juoserver.game.skill.flow.start.StartSkillActionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CalculateSkillActionDurationStepTest {
    @ParameterizedTest
    @CsvSource({
            "-10, 2.0",
            "0, 2.0",
            "50, 1.3333333333",
            "100, 1.0",
            "300, 0.5",
            "700, 0.25",
            "1900, 0.1"
    })
    void baseDurationAppliesAtOneHundredDexterityAndScalesWithoutLimits(int dexterity, double expectedSeconds) {
        var player = mock(UOPlayer.class);
        when(player.getDexterity()).thenReturn(dexterity);
        var context = new StartSkillActionContext(player, SkillActionOptions.builder().build());

        new CalculateSkillActionDurationStep().execute(context);

        assertEquals(expectedSeconds, context.getDurationSeconds(), 1e-9);
    }
}
