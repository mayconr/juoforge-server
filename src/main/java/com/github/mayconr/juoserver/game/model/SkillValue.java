package com.github.mayconr.juoserver.game.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
@EqualsAndHashCode
public class SkillValue {
    @EqualsAndHashCode.Include
    private final UUID id;
    private final int skillId;
    private double base;   // real skill
    private double value;  // effective skill (buff/debuff)
    private double cap;
    private SkillLock lock;

    public static SkillValue of(UUID id, int skillId, double base, double cap, SkillLock lock) {
        return new SkillValue(id, skillId, base, base, cap, lock);
    }

    public static SkillValue of(UUID id, SkillValue value) {
        return new SkillValue(id, value.skillId, value.base, value.value, value.cap, value.lock);
    }

    public static SkillValue of(int skillId, double base, double cap) {
        return new SkillValue(UUID.randomUUID(), skillId, base, base, cap, SkillLock.UP);
    }

    public static SkillValue of(int skillId, SkillLock lock) {
        return new SkillValue(UUID.randomUUID(), skillId, 0, 0, 0, lock);
    }

    public static SkillValue zero(int skillId) {
        return new SkillValue(UUID.randomUUID(), skillId, 0,0,0, SkillLock.UP);
    }

    public void increase(double amount) {
        if (lock != SkillLock.UP) return;
        base = Math.min(base + amount, cap);
        recalcValue();
    }

    public void decrease(double amount) {
        if (lock != SkillLock.DOWN) return;
        base = Math.max(0, base - amount);
        recalcValue();
    }

    public void applyModifier(double delta) {
        value = Math.max(0, value + delta);
    }

    public void resetModifiers() {
        value = base;
    }

    public void setBase(double value) {
        this.base = Math.max(0, Math.min(value, cap));
        recalcValue();
    }

    public void setCap(double cap) {
        this.cap = Math.max(0, cap);
        if (base > cap) base = cap;
        recalcValue();
    }

    private void recalcValue() {
        this.value = Math.max(0, base);
    }

    // GETTERS
    public double getBase() {
        return base;
    }

    public double getValue() {
        return value;
    }

    public double getCap() {
        return cap;
    }

    public boolean isCapped() {
        return base >= cap;
    }

    public boolean canIncrease() {
        return lock == SkillLock.UP && base < cap;
    }

    public boolean canDecrease() {
        return lock == SkillLock.DOWN && base > 0;
    }
}
