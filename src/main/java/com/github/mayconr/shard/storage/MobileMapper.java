package com.github.mayconr.shard.storage;

import com.github.mayconr.juoserver.game.model.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.UUID;

@Mapper
public interface MobileMapper {

    Integer findNextMobileSerial();

    int updateMobileSerial(@Param("serial") long serial);

    boolean mobileExists(@Param("name") String name);

    UOMobile findMobileBySerialId(@Param("serialId") int serialId);

    UOMobile findMobileById(@Param("id") UUID id);

    List<UONpc> findAllNpcs();

    List<AccountMobile> findAccountMobilesByAccountId(@Param("accountId") UUID accountId);

    List<SkillValue> findSkillsByMobileId(@Param("mobileId") UUID mobileId);

    int upsertMobile(UOMobile mobile);

    int upsertPlayer(UOPlayer player);

    int upsertNpc(UONpc npc);

    int deleteById(@Param("id") UUID id);

    int deleteBySerialId(@Param("serialId") Integer serialId);

    int upsertMobileRuntime(UOMobile mobile);

    int upsertMobileVitals(UOMobile mobile);

    int upsertMobileAttributes(UOMobile mobile);

    void upsertSkill(@Param("mobile") UOMobile mobile, @Param("skill") SkillValue skill);
}
