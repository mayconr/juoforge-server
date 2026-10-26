package com.github.mayconr.juoserver.game.mobile.flow.mount.validation;

import com.github.mayconr.juoserver.game.mobile.flow.mount.MountContext;
import com.github.mayconr.juoserver.game.mobile.template.MountTemplate;
import com.github.mayconr.juoserver.game.model.Layer;
import com.github.mayconr.juoserver.game.model.GameMath;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import com.github.mayconr.juoserver.infrastructure.template.TemplateRegistry;

public class ValidateMountStep extends AbstractFlowStep<MountContext> {

    private final TemplateRegistry<String, MountTemplate> mountTemplateByNpcName;
    private final int mountRange;

    public ValidateMountStep(TemplateRegistry<String, MountTemplate> mountTemplateByNpcName, int mountRange) {
        super("ValidateMount");
        this.mountTemplateByNpcName = mountTemplateByNpcName;
        this.mountRange = mountRange;
    }

    @Override
    public StepResult execute(MountContext context) {
        final var mobile = context.getMobile();
        final var mountNpc = context.getMountNpc();

        if (mobile == null) {
            return StepResult.failure("MOUNT_MOBILE_REQUIRED", "Mobile is null", "{mount.invalid_target}");
        }

        if (mountNpc == null) {
            return StepResult.failure("MOUNT_NPC_REQUIRED", "Npc is null", "{mount.invalid_target}");
        }

        if (mobile.getEquippedItems().containsKey(Layer.MOUNT)) {
            return StepResult.failure("ALREADY_MOUNTED", "Mobile is already mounted", "{mount.already_mounted}");
        }

        if (!GameMath.isInRange(mobile, mountNpc, mountRange)) {
            return StepResult.failure("MOUNT_OUT_OF_RANGE", "Mount is outside the allowed range of " + mountRange,
                    "{mount.out_of_range}");
        }

        if (mountTemplateByNpcName.get(mountNpc.getName()).isEmpty()) {
            return StepResult.failure("MOUNT_TEMPLATE_NOT_FOUND", "Npc "+mountNpc.getName()+" does not have mount configured.",
                    "{mount.not_mountable}");
        }
        return StepResult.success();
    }
}
