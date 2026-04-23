package com.github.mayconr.juoserver.game.flow;

import com.github.mayconr.juoserver.game.GamePlaySettings;
import com.github.mayconr.juoserver.game.item.ItemModule;
import com.github.mayconr.juoserver.game.item.ItemRequest;
import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.game.player.flow.creation.*;
import com.github.mayconr.juoserver.game.player.template.BodyKey;
import com.github.mayconr.juoserver.game.player.template.BodyTemplate;
import com.github.mayconr.juoserver.game.player.template.StartkitTemplate;
import com.github.mayconr.juoserver.game.world.SerialGenerator;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.flow.AsyncFlowContext;
import com.github.mayconr.juoserver.infrastructure.flow.Flow;
import com.github.mayconr.juoserver.infrastructure.flow.FlowFactory;
import com.github.mayconr.juoserver.infrastructure.region.RegionNode;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;
import com.github.mayconr.juoserver.infrastructure.template.TemplateRegistry;
import com.github.mayconr.juoserver.network.packet.CreateCharacter;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

public final class PlayerCreationFlowDefinition {
    private PlayerCreationFlowDefinition() {
    }

    public static Flow<PlayerCreationContext> build(RealmStorage storage, EventBus eventBus,
                                                    TemplateRegistry<BodyKey, BodyTemplate> registry,
                                                    GamePlaySettings settings,
                                                    TemplateRegistry<Integer, StartkitTemplate> startKitTemplateBySkillId,
                                                    ItemModule itemModule, SerialGenerator serialGenerator) {
        return FlowFactory.<PlayerCreationContext>builder()
                .step(new ValidatePlayerNameStep(storage))
                .step(new ResolveStartingLocationStep())
                .step(new ResolveBodyTemplateStep(registry))
                .step(new BuildInitialSkillsStep(settings))
                .step(new BuildStartKitStep(settings, startKitTemplateBySkillId))
                .step(new CreateEquippedItemsStep(itemModule))
                .step(new CreateMobileDataStep(serialGenerator, storage))
                .step(new CreateMobileStep(storage))
                .step(new PersistPlayerStep(storage, serialGenerator))
                .build();
    }

    @EqualsAndHashCode(callSuper = true)
    @RequiredArgsConstructor
    @Data
    public static class PlayerCreationContext extends AsyncFlowContext<UOPlayer> {
        private final CreateCharacter character;
        private final Map<Integer, RegionNode> startingLocations;
        private final UOAccount account;

        private RegionNode startingLocation;
        private BodyTemplate bodyTemplate;
        private Map<Integer, SkillValue> skills;
        private List<ItemRequest> startKitRequests;
        private Map<Layer, Integer> equippedItems;
        private List<Integer> backpackItems;
        private UOMobileData mobileData;
        private UOPlayer player;
    }
}
