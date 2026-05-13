package com.github.mayconr.juoserver.game.player.flow.creation;

import com.github.mayconr.juoserver.game.GamePlaySettings;
import com.github.mayconr.juoserver.game.item.ItemModule;
import com.github.mayconr.juoserver.game.player.flow.creation.build.BuildInitialSkillsStep;
import com.github.mayconr.juoserver.game.player.flow.creation.build.CreateStarterItemsStep;
import com.github.mayconr.juoserver.game.player.flow.creation.factory.CreateMobileDataStep;
import com.github.mayconr.juoserver.game.player.flow.creation.factory.CreateMobileStep;
import com.github.mayconr.juoserver.game.player.flow.creation.persistence.SavePlayerStep;
import com.github.mayconr.juoserver.game.player.flow.creation.resolve.ResolveBodyTemplateStep;
import com.github.mayconr.juoserver.game.player.flow.creation.resolve.ResolveStartingLocationStep;
import com.github.mayconr.juoserver.game.player.flow.creation.validation.ValidatePlayerNameStep;
import com.github.mayconr.juoserver.game.player.template.BodyKey;
import com.github.mayconr.juoserver.game.player.template.BodyTemplate;
import com.github.mayconr.juoserver.game.player.template.StartKitTemplate;
import com.github.mayconr.juoserver.game.world.SerialGenerator;
import com.github.mayconr.juoserver.game.world.context.FlowRegistryFactory;
import com.github.mayconr.juoserver.game.world.context.FlowRegistryFactory.GameInfra;
import com.github.mayconr.juoserver.game.world.context.FlowRegistryFactory.GameModules;
import com.github.mayconr.juoserver.game.world.context.FlowRegistryFactory.GameTemplates;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.flow.Flow;
import com.github.mayconr.juoserver.infrastructure.flow.FlowFactory;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;
import com.github.mayconr.juoserver.infrastructure.template.TemplateRegistry;

public final class PlayerCreationFlowDefinition {
    private PlayerCreationFlowDefinition() {
    }

    public static Flow<PlayerCreationContext> build(GameModules modules, GameInfra infra, GameTemplates templates) {
        return FlowFactory.<PlayerCreationContext>builder()
                .step(new ValidatePlayerNameStep(infra.storage()))
                .step(new ResolveStartingLocationStep())
                .step(new ResolveBodyTemplateStep(templates.bodyByKey()))
                .step(new BuildInitialSkillsStep(infra.settings()))
                .step(new CreateStarterItemsStep(infra.settings(), templates.startKitBySkillId(), modules.item()))
                .step(new CreateMobileDataStep(infra.serialGenerator(), infra.storage()))
                .step(new CreateMobileStep(infra.storage()))
                .step(new SavePlayerStep(infra.storage(), infra.serialGenerator()))
                .build();
    }

}
