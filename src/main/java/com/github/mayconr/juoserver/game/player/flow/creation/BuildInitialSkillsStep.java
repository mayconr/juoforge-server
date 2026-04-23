package com.github.mayconr.juoserver.game.player.flow.creation;

import com.github.mayconr.juoserver.game.GamePlaySettings;
import com.github.mayconr.juoserver.game.flow.PlayerCreationFlowDefinition.PlayerCreationContext;
import com.github.mayconr.juoserver.game.model.SkillValue;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

import java.util.List;
import java.util.Map;

public class BuildInitialSkillsStep extends AbstractFlowStep<PlayerCreationContext> {

    private final GamePlaySettings settings;

    public BuildInitialSkillsStep(GamePlaySettings settings) {
        super("BuildInitialSkills");
        this.settings = settings;
    }

    @Override
    public StepResult execute(PlayerCreationContext context) {
        var c = context.getCharacter();

        // TODO validate supported skills
        var skills = Map.of(c.getSkill1(), SkillValue.of(c.getSkill1(), c.getSkill1Value(), settings.skills().cap()),
                c.getSkill2(), SkillValue.of(c.getSkill2(), c.getSkill2Value(), settings.skills().cap()),
                c.getSkill3(), SkillValue.of(c.getSkill3(), c.getSkill3Value(), settings.skills().cap()));

        context.setSkills(skills);
        return StepResult.success();
    }
}
