package com.github.mayconr.juoserver.game.model;

import lombok.ToString;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ToString(onlyExplicitlyIncluded = true)
public class SkillContainer {

    @ToString.Include
    private final Map<Integer, SkillValue> skillMap = new ConcurrentHashMap<>();

    public SkillContainer() {
    }

    public SkillContainer(Collection<SkillValue> skills) {
        for (SkillValue value : skills) {
            set(value.getSkillId(), value);
        }
    }

    public SkillValue get(Integer id) {
        return skillMap.computeIfAbsent(id, SkillValue::zero);
    }

    public void set(Integer id, SkillValue value) {
        skillMap.put(id, value);
    }

    public SkillValue updateSkill(int id, SkillLock lock) {
        return skillMap.compute(id, (integer, value) -> {
            if (value == null) {
                return SkillValue.of(id, lock);
            }
            value.setLock(lock);
            return value;
        });
    }

    public Collection<SkillValue> getSkillValues() {
        return List.copyOf(skillMap.values());
    }

    public Map<Integer, SkillValue> getSkillMap() {
        return Map.copyOf(skillMap);
    }
}
