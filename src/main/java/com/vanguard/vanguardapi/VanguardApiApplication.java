package com.vanguard.vanguardapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class VanguardApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(VanguardApiApplication.class, args);
    }

}
