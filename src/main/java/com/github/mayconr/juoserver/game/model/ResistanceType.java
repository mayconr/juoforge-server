package com.github.mayconr.juoserver.game.model;

/** The five resistance channels supported by mobiles and items. */
public enum ResistanceType {
    PHYSICAL,
    FIRE,
    COLD,
    POISON,
    ENERGY;

    public int getValue(UOMobile mobile) {
        return switch (this) {
            case PHYSICAL -> mobile.getPhysicalResist();
            case FIRE -> mobile.getFireResist();
            case COLD -> mobile.getColdResist();
            case POISON -> mobile.getPoisonResist();
            case ENERGY -> mobile.getEnergyResist();
        };
    }

    public void setValue(UOMobile mobile, int value) {
        switch (this) {
            case PHYSICAL -> mobile.setPhysicalResist(value);
            case FIRE -> mobile.setFireResist(value);
            case COLD -> mobile.setColdResist(value);
            case POISON -> mobile.setPoisonResist(value);
            case ENERGY -> mobile.setEnergyResist(value);
        }
    }
}
