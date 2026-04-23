package com.github.mayconr.juoserver.game.damage.flow.damage;

import com.github.mayconr.juoserver.game.flow.DamageFlowDefinition.DamageContext;
import com.github.mayconr.juoserver.game.mobile.MobileModule;
import com.github.mayconr.juoserver.game.model.DeathCause;
import com.github.mayconr.juoserver.game.model.DeathRequest;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.FlowPhase;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

public class CheckLethalDamageStep extends AbstractFlowStep<DamageContext> {

    private final MobileModule mobileModule;

    public CheckLethalDamageStep(MobileModule mobileModule) {
        super("check_lethal_damage", 300, FlowPhase.CORE);
        this.mobileModule = mobileModule;
    }

    @Override
    public StepResult execute(DamageContext context) {
        context.setLethal(context.getNewHp() == 0);

        if (context.isLethal()) {
            mobileModule.die(new DeathRequest(context.getTarget(), context.getSource(), DeathCause.COMBAT));
        }

        return StepResult.success();
    }
}
