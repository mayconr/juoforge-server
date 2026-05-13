package com.github.mayconr.juoserver.game.mobile.flow.mount.creation;

import com.github.mayconr.juoserver.game.mobile.flow.mount.MountContext;
import com.github.mayconr.juoserver.game.item.ItemModule;
import com.github.mayconr.juoserver.game.item.ItemRequest;
import com.github.mayconr.juoserver.game.mobile.template.MountTemplate;
import com.github.mayconr.juoserver.game.model.ItemTarget;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import com.github.mayconr.juoserver.infrastructure.template.TemplateRegistry;

public class CreateMountItemStep extends AbstractFlowStep<MountContext> {

    private final ItemModule itemModule;
    private final TemplateRegistry<String, MountTemplate> mountTemplateByNpcName;

    public CreateMountItemStep(ItemModule itemModule, TemplateRegistry<String, MountTemplate> mountTemplateByNpcName) {
        super("CreateMountItem");
        this.itemModule = itemModule;
        this.mountTemplateByNpcName = mountTemplateByNpcName;
    }

    @Override
    public StepResult execute(MountContext context) {
        final var mountNpc = context.getMountNpc();

        final var mountTemplate = mountTemplateByNpcName.get(mountNpc.getName()).stream().findFirst().orElse(null);

        if (mountTemplate == null) {
            return StepResult.failure("Mount template not found for NPC: " + mountNpc.getName());
        }

        var item = itemModule.createItem(ItemRequest.byName(mountTemplate.itemName()), ItemTarget.orphan());

        context.setMountItem(item);

        return StepResult.success();
    }
}
