package com.github.mayconr.juoserver.game.mobile.flow.mount.equip;

import com.github.mayconr.juoserver.game.mobile.flow.mount.MountContext;
import com.github.mayconr.juoserver.game.mobile.MobileModule;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

public class EquipMountItemStep extends AbstractFlowStep<MountContext> {

    private final MobileModule mobileModule;

    public EquipMountItemStep(MobileModule mobileModule) {
        super("EquipMountItem");
        this.mobileModule = mobileModule;
    }

    @Override
    public StepResult execute(MountContext context) {
        final var mobile = context.getMobile();
        final var mountItem = context.getMountItem();

        mobileModule.equipItem(mobile, mountItem);

        return StepResult.success();
    }
}
