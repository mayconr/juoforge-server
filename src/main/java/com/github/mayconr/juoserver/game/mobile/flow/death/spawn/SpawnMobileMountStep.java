package com.github.mayconr.juoserver.game.mobile.flow.death.spawn;

import com.github.mayconr.juoserver.game.mobile.flow.death.DeathContext;
import com.github.mayconr.juoserver.game.mobile.template.MountTemplate;
import com.github.mayconr.juoserver.game.npc.NpcRequester;
import com.github.mayconr.juoserver.game.npc.WorldNpc;
import com.github.mayconr.juoserver.infrastructure.template.TemplateRegistry;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SpawnMobileMountStep extends AbstractFlowStep<DeathContext> {
    private final WorldNpc npcs;
    private final TemplateRegistry<String, MountTemplate> mountsByItemName;

    public SpawnMobileMountStep(WorldNpc npcs, TemplateRegistry<String, MountTemplate> mountsByItemName) {
        super("spawn_mobile_mount_step");
        this.npcs = npcs;
        this.mountsByItemName = mountsByItemName;
    }

    @Override
    public StepResult execute(DeathContext context) {
        var mountItem = context.getMountItem();
        if (mountItem == null) {
            return StepResult.skip();
        }

        var template = mountsByItemName.get(mountItem.getName()).stream().findFirst().orElse(null);
        if (template == null) {
            log.warn("Cannot restore mount: no mount template for item {}", mountItem.getName());
            return StepResult.skip("MOUNT_TEMPLATE_NOT_FOUND", "Mount template not found for " + mountItem.getName());
        }

        // Mount items identify a template, not the original NPC instance.
        var npc = npcs.createNpc(new NpcRequester.AsyncProcess("death-unmount"),
                template.npcName(), context.getVictim());
        if (npc == null) {
            log.warn("Cannot restore mount NPC {} after death", template.npcName());
            return StepResult.skip("MOUNT_CREATION_FAILED", "Cannot create mount " + template.npcName());
        }
        context.setMountItem(null);
        return StepResult.success();
    }
}
