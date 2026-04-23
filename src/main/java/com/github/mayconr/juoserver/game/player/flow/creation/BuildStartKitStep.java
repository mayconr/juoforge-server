package com.github.mayconr.juoserver.game.player.flow.creation;

import com.github.mayconr.juoserver.game.GamePlaySettings;
import com.github.mayconr.juoserver.game.flow.PlayerCreationFlowDefinition.PlayerCreationContext;
import com.github.mayconr.juoserver.game.item.ItemRequest;
import com.github.mayconr.juoserver.game.player.template.StartkitTemplate;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import com.github.mayconr.juoserver.infrastructure.template.TemplateRegistry;

import java.util.ArrayList;
import java.util.List;

import static com.github.mayconr.juoserver.game.item.ItemRequest.byModelId;
import static com.github.mayconr.juoserver.game.item.ItemRequest.byName;

public class BuildStartKitStep extends AbstractFlowStep<PlayerCreationContext> {

    private final GamePlaySettings settings;
    private final TemplateRegistry<Integer, StartkitTemplate> startKitTemplateBySkillId;

    public BuildStartKitStep(GamePlaySettings settings,
                             TemplateRegistry<Integer, StartkitTemplate> startKitTemplateBySkillId) {
        super("buildStartKit");
        this.settings = settings;
        this.startKitTemplateBySkillId = startKitTemplateBySkillId;
    }

    @Override
    public StepResult execute(PlayerCreationContext ctx) {
        var c = ctx.getCharacter();
        var kit = new ArrayList<ItemRequest>();

        kit.add(byName("shirt").withHue(c.getShirtColor()));
        kit.add(byName("pants").withHue(c.getPantsColor()));
        kit.add(byName("shoes"));
        kit.add(byModelId(c.getHairStyle()).withHue(c.getHairColor()));
        kit.add(byModelId(c.getBeardStyle()).withHue(c.getBeardColor()));
        kit.add(byName(settings.mobile().backpackItem()));

        addTemplates(kit, startKitTemplateBySkillId.get(0)); // 0 = no skill needed
        addTemplates(kit, startKitTemplateBySkillId.get(c.getSkill1()));
        addTemplates(kit, startKitTemplateBySkillId.get(c.getSkill2()));
        addTemplates(kit, startKitTemplateBySkillId.get(c.getSkill3()));

        ctx.setStartKitRequests(List.copyOf(kit));
        return StepResult.success();
    }

    private void addTemplates(List<ItemRequest> kit, List<StartkitTemplate> templates) {
        for (var t : templates) {
            kit.add(byName(t.item()).withAmount(t.amount()));
        }
    }
}
