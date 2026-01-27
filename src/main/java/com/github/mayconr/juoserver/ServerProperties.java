package com.github.mayconr.juoserver;

import lombok.Data;

@Data
public class ServerProperties {

    private Vitals vitals;

    @Data
    public static class Vitals {
        /** Controls how quickly regen approaches its maximum as the stat increases. */
        private int saturationFactor = 100;
    }

}
