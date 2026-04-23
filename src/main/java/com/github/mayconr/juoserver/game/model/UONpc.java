package com.github.mayconr.juoserver.game.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class UONpc extends UOMobile {
    private int speechHue;
    private int speechFont;
    private BehaviorDefinition behavior;
    private List<String> roles;

    public UONpc(UOMobileData data) {
        super(data);
        this.speechHue = data.getSpeechHue();
        this.speechFont = data.getSpeechFont();
        this.behavior = data.getBehavior();
        this.roles = data.getRoles();
    }

    @Override
    protected void populateData(UOMobileData data) {
        super.populateData(data);
        data.setSpeechHue(speechHue);
        data.setSpeechFont(speechFont);
        data.setBehavior(behavior);
        data.setRoles(roles);
    }
}
