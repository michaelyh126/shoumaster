package com.yh.shou;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude= {DataSourceAutoConfiguration.class})

public class ShouApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShouApplication.class, args);
    }

}
