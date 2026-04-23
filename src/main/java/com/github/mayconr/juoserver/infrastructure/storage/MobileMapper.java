package com.github.mayconr.juoserver.infrastructure.storage;

import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.model.UOMobileData;
import com.github.mayconr.juoserver.game.model.UONpc;
import com.github.mayconr.juoserver.game.model.UOPlayer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MobileMapper {

    public static UOMobile mapToMobile(UOMobileData data) {
        return switch (data.getType()) {
            case "N" -> new UONpc(data);
            case "P" -> new UOPlayer(data);
            default -> throw new IllegalArgumentException("Unknown UOMobile type");
        };
    }

    public static List<UOMobile> mapToMobile(Collection<UOMobileData> datas) {
        List<UOMobile> mobiles = new ArrayList<>();
        for (UOMobileData data : datas) {
            mobiles.add(mapToMobile(data));
        }
        return mobiles;
    }
}
