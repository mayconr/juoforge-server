package com.github.mayconr.juoserver.game.mobile.shared;

import com.github.mayconr.juoserver.game.model.ResistanceType;
import com.github.mayconr.juoserver.game.mobile.WorldMobile;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.infrastructure.flow.FlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import com.github.mayconr.juoserver.infrastructure.storage.WorldStorage;
import java.util.function.Function;

/** Applies starter equipment once, after a new mobile is created and before it is exposed. */
public final class InitializeEquipmentResistancesStep<T> implements FlowStep<T> {
    private final Function<T, UOMobile> mobileResolver;
    private final WorldStorage storage;
    private final WorldMobile mobiles;

    public InitializeEquipmentResistancesStep(Function<T, UOMobile> mobileResolver,
                                              WorldStorage storage, WorldMobile mobiles) {
        this.mobileResolver = mobileResolver;
        this.storage = storage;
        this.mobiles = mobiles;
    }

    @Override
    public String name() {
        return "initialize-equipment-resistances";
    }

    @Override
    public StepResult execute(T context) {
        var mobile = mobileResolver.apply(context);
        var items = mobile.getEquippedItems().values().stream()
                .map(serial -> storage.items().get(serial)
                        .orElseThrow(() -> new IllegalStateException("Equipped item not found: " + serial)))
                .toList();
        for (var type : ResistanceType.values()) {
            int value = type.getValue(mobile);
            for (var item : items) {
                value += item.getResistances().getValue(type);
            }
            mobiles.setResistance(mobile, type, value);
        }
        return StepResult.success();
    }
}
