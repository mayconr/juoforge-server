package com.github.mayconr.shard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.github.mayconr.shard")
public class ApplicationMain {

    public static void main(String[] args) {
        System.out.println((int) 0x56C5);
        SpringApplication.run(ApplicationMain.class, args);
    }

}
