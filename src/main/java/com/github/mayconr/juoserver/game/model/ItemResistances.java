package com.github.mayconr.juoserver.game.model;

/** Base resistance contributions of one item, each in [0, 100]. */
public record ItemResistances(int physical, int fire, int cold, int poison, int energy) {
    public static final ItemResistances NONE = new ItemResistances(0, 0, 0, 0, 0);

    public ItemResistances {
        validate("physical", physical);
        validate("fire", fire);
        validate("cold", cold);
        validate("poison", poison);
        validate("energy", energy);
    }

    public int getValue(ResistanceType type) {
        return switch (type) {
            case PHYSICAL -> physical;
            case FIRE -> fire;
            case COLD -> cold;
            case POISON -> poison;
            case ENERGY -> energy;
        };
    }

    private static void validate(String type, int value) {
        if (value < 0 || value > 100) {
            throw new IllegalArgumentException(type + " resistance must be between 0 and 100: " + value);
        }
    }
}
