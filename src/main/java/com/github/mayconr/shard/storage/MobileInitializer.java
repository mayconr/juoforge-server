package com.github.mayconr.shard.storage;

import com.github.mayconr.juoserver.game.model.SkillContainer;
import com.github.mayconr.juoserver.game.model.UOMobile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class MobileInitializer {

    private final MobileMapper mobileMapper;

    public void initialize(UOMobile mobile) {
        log.debug("Loading skills for mobile [{}]", mobile.getId());
        var skills = mobileMapper.findSkillsByMobileId(mobile.getId());
        mobile.setSkills(new SkillContainer(skills));
    }
}
