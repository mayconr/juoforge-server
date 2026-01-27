package com.github.mayconr.juoserver;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;
import org.springframework.boot.context.properties.bind.DefaultValue;

@Data
@ConfigurationProperties(prefix = "server")
public class ServerProperties {

    private final Vitals vitals;

    @ConstructorBinding
    public ServerProperties(Vitals vitals) {
        this.vitals = vitals;
    }

    /**
     * @param saturationFactor Controls how quickly regen approaches its maximum as the stat increases.
     */
    public record Vitals(int saturationFactor) {

            @ConstructorBinding
            public Vitals(@DefaultValue("100") int saturationFactor) {
                this.saturationFactor = saturationFactor;
            }
        }

}
