package com.github.mayconr.juoserver.game.mobile.flow.unmount.load;

import com.github.mayconr.juoserver.game.mobile.flow.unmount.UnmountContext;
import com.github.mayconr.juoserver.game.mobile.template.MountTemplate;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import com.github.mayconr.juoserver.infrastructure.template.TemplateRegistry;

public class LoadMountNpcNameStep extends AbstractFlowStep<UnmountContext> {

    private final TemplateRegistry<String, MountTemplate> mountTemplateByItemName;

    public LoadMountNpcNameStep(TemplateRegistry<String, MountTemplate> mountTemplateByItemName) {
        super("LoadMountNpcTemplate");
        this.mountTemplateByItemName = mountTemplateByItemName;
    }

    @Override
    public StepResult execute(UnmountContext context) {
        final var mountItem = context.getMountItem();

        var mountTemplate = mountTemplateByItemName.get(mountItem.getName()).stream().findFirst().orElse(null);

        if (mountTemplate == null) {
            return StepResult.failure("Mount template not found for item " + mountItem.getName());
        }

        context.setMountNpcName(mountTemplate.npcName());

        return StepResult.success();
    }
}
