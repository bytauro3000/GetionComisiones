package com.vendelote;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class VendeLoteApplication {

    public static void main(String[] args) {
        SpringApplication.run(VendeLoteApplication.class, args);
    }
}
