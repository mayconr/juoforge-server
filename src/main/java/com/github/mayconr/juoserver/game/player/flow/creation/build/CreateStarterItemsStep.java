package com.github.mayconr.juoserver.game.player.flow.creation.build;

import com.github.mayconr.juoserver.game.GamePlaySettings;
import com.github.mayconr.juoserver.game.player.flow.creation.PlayerCreationContext;
import com.github.mayconr.juoserver.game.item.ItemModule;
import com.github.mayconr.juoserver.game.item.ItemRequest;
import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.game.player.template.StartKitTemplate;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import com.github.mayconr.juoserver.infrastructure.template.TemplateRegistry;

import java.util.ArrayList;
import java.util.List;

import static com.github.mayconr.juoserver.game.item.ItemRequest.byModelId;
import static com.github.mayconr.juoserver.game.item.ItemRequest.byName;

public class CreateStarterItemsStep extends AbstractFlowStep<PlayerCreationContext> {

    private final GamePlaySettings settings;
    private final TemplateRegistry<Integer, StartKitTemplate> startKitTemplateBySkillId;
    private final ItemModule itemModule;

    public CreateStarterItemsStep(GamePlaySettings settings,
                                  TemplateRegistry<Integer, StartKitTemplate> startKitTemplateBySkillId, ItemModule itemModule) {
        super("CreateStarterItems");
        this.settings = settings;
        this.startKitTemplateBySkillId = startKitTemplateBySkillId;
        this.itemModule = itemModule;
    }

    @Override
    public StepResult execute(PlayerCreationContext ctx) {
        var c = ctx.getCharacter();
        var startKit = new ArrayList<ItemRequest>();

        startKit.add(byName("shirt").withHue(c.getShirtColor()));
        startKit.add(byName("pants").withHue(c.getPantsColor()));
        startKit.add(byName("shoes"));
        if (c.getHairStyle() != 0) {
            startKit.add(byModelId(c.getHairStyle()).withHue(c.getHairColor()));
        }
        if (c.getBeardStyle() != 0) {
            startKit.add(byModelId(c.getBeardStyle()).withHue(c.getBeardColor()));
        }
        startKit.add(byName(settings.mobile().backpackItem()));

        addTemplates(startKit, startKitTemplateBySkillId.get(-1)); // -1 = no skill needed
        addTemplates(startKit, startKitTemplateBySkillId.get(c.getSkill1()));
        addTemplates(startKit, startKitTemplateBySkillId.get(c.getSkill2()));
        addTemplates(startKit, startKitTemplateBySkillId.get(c.getSkill3()));

        var starterItems = new ArrayList<UOItem>();
        for (ItemRequest request : startKit) {
            var item = itemModule.createItem(request, ItemTarget.orphan());
            starterItems.add(item);

            // Backpack item created
            if (item.hasFlag(ItemFlag.CONTAINER) && Layer.BACKPACK.equals(item.getLayer()) && item instanceof UOContainer container) {
                ctx.setBackpack(container);
            }
        }
        ctx.setStarterItems(starterItems);

        return StepResult.success();
    }

    private void addTemplates(List<ItemRequest> kit, List<StartKitTemplate> templates) {
        for (var t : templates) {
            kit.add(byName(t.item()).withAmount(t.amount()));
        }
    }
}
