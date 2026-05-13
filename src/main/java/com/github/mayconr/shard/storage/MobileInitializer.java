package com.github.mayconr.shard.storage;

import com.github.mayconr.juoserver.game.model.SkillContainer;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.model.UOMobileData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;

@Slf4j
@RequiredArgsConstructor
public class MobileInitializer {

    private final MobileMapper mobileMapper;

    public void initialize(UOMobileData mobile) {
        log.debug("Loading skills for mobile [{}]", mobile.getSerialId());
        //var skills = mobileMapper.findSkillsByMobileId(mobile.get)
        //mobile.setSkills(new SkillContainer(Collections.emptyList()));
        // TODO load skills
    }
}
