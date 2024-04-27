package com.weTalk;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(scanBasePackages = {"com.weTalk"}, exclude = DataSourceAutoConfiguration.class)
public class WeTalkApplication {
    public static void main(String[] args) {
        SpringApplication.run(WeTalkApplication.class, args);
    }
}
