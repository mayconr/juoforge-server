package com.github.mayconr.shard.storage;

import com.github.mayconr.juoserver.game.model.SkillValue;
import com.github.mayconr.juoserver.game.model.UOMobileData;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MobileMapper {

    // =========================
    // Serial
    // =========================

    Integer findNextMobileSerial();

    int updateMobileSerial(@Param("serial") Integer serial);


    // =========================
    // Queries
    // =========================

    boolean mobileExists(@Param("name") String name);

    UOMobileData findMobileBySerialId(@Param("serialId") Integer serialId);

    List<UOMobileData> findAllNpcs();

    List<SkillValue> findSkillsBySerialId(@Param("serialId") Integer serialId);


    // =========================
    // Write (tabela única)
    // =========================

    int upsertMobile(UOMobileData mobile);


    // =========================
    // Skills
    // =========================

    int upsertSkill(@Param("serialId") Integer serialId,
                    @Param("skill") SkillValue skill);


    // =========================
    // Delete
    // =========================

    int deleteBySerialId(@Param("serialId") Integer serialId);
}
