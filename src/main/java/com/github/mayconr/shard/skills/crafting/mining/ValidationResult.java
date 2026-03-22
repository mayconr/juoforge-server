package com.github.mayconr.shard.skills.crafting.mining;

public record ValidationResult(boolean isValid, String message) {

    public static ValidationResult valid() {
        return new ValidationResult(true, null);
    }

    public static ValidationResult invalid(String message) {
        return new ValidationResult(false, message);
    }
}
