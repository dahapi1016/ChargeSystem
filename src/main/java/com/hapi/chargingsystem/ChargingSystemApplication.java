package com.hapi.chargingsystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
//@MapperScan("com.hapi.chargingsystem.mapper")
public class ChargingSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChargingSystemApplication.class, args);
    }

}
