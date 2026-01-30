package com.github.mayconr.shard;

import com.github.mayconr.juoserver.EnableJuoforge;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@EnableJuoforge
@SpringBootApplication
@ComponentScan(basePackages = "com.github.mayconr")
public class ApplicationMain {

    public static void main(String[] args) {
        SpringApplication.run(ApplicationMain.class, args);
    }

}
