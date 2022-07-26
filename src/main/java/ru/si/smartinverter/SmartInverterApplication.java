package ru.si.smartinverter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
public class SmartInverterApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartInverterApplication.class, args);
    }

}
