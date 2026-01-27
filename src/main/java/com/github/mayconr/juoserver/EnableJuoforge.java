package com.github.mayconr.juoserver;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({
        WorldConfig.class,
        NetworkConfig.class
})
public @interface EnableJuoforge {
}
