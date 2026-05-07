package com.github.mayconr.juoserver.game.mobile.flow.mount.validation;

import com.github.mayconr.juoserver.game.mobile.flow.mount.MountContext;
import com.github.mayconr.juoserver.game.mobile.template.MountTemplate;
import com.github.mayconr.juoserver.game.model.Layer;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import com.github.mayconr.juoserver.infrastructure.template.TemplateRegistry;

public class ValidateMountStep extends AbstractFlowStep<MountContext> {

    private final TemplateRegistry<String, MountTemplate> mountTemplateByNpcName;

    public ValidateMountStep(TemplateRegistry<String, MountTemplate> mountTemplateByNpcName) {
        super("ValidateMount");
        this.mountTemplateByNpcName = mountTemplateByNpcName;
    }

    @Override
    public StepResult execute(MountContext context) {
        final var mobile = context.getMobile();
        final var mountNpc = context.getMountNpc();

        if (mobile == null) {
            return StepResult.failure("Mobile is null");
        }

        if (mountNpc == null) {
            return StepResult.failure("Npc is null");
        }

        if (mobile.getEquippedItems().containsKey(Layer.MOUNT)) {
            return StepResult.failure("Mobile is already mounted");
        }

        if (mountTemplateByNpcName.get(mountNpc.getName()).isEmpty()) {
            return StepResult.failure("Npc "+mountNpc.getName()+" does not have mount configured.");
        }
        return StepResult.success();
    }
}
