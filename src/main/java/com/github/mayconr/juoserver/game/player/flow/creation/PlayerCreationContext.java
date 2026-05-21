package com.github.mayconr.juoserver.game.player.flow.creation;

import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.game.player.template.BodyTemplate;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractAsyncFlowContext;
import com.github.mayconr.juoserver.infrastructure.region.RegionNode;
import com.github.mayconr.juoserver.network.packet.CreateCharacter;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@RequiredArgsConstructor
@Data
public class PlayerCreationContext extends AbstractAsyncFlowContext<UOPlayer> {
    private final CreateCharacter character;
    private final Map<Integer, RegionNode> startingLocations;
    private final UOAccount account;

    private RegionNode startingLocation;
    private BodyTemplate bodyTemplate;
    private Map<Integer, SkillValue> skills;

    private List<UOItem> starterItems;
    private UOContainer backpack;

    private UOMobileData mobileData;
    private UOPlayer player;
}
