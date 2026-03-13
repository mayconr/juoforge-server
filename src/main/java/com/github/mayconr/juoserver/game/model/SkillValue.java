package com.github.mayconr.juoserver.game.model;

import lombok.Data;

@Data
public class SkillValue {

    private final int skillId;
    // GETTERS
    private double base;   // real skill
    private double value;  // effective skill (buff/debuff)
    private double cap;
    private SkillLock lock;

    public SkillValue(Integer skillId, Double base, Double cap, SkillLock lock) {
        this.skillId = skillId;
        this.base = base;
        this.value = base;
        this.cap = cap;
        this.lock = lock;
    }

    public static SkillValue of(int skillId, double base, double cap, SkillLock lock) {
        return new SkillValue(skillId, base, cap, lock);
    }

    public static SkillValue of(SkillValue value) {
        return new SkillValue(value.skillId, value.base, value.cap, value.lock);
    }

    public static SkillValue of(int skillId, double base, double cap) {
        return new SkillValue(skillId, base, cap, SkillLock.UP);
    }

    public static SkillValue of(int skillId, SkillLock lock) {
        return new SkillValue(skillId, 0d, 0d, lock);
    }

    public static SkillValue zero(int skillId) {
        return new SkillValue(skillId, 0d,0d, SkillLock.UP);
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
