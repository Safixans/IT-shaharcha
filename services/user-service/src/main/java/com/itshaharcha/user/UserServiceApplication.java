package com.itshaharcha.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Scan com.itshaharcha so shared common-lib components register.
@SpringBootApplication(scanBasePackages = "com.itshaharcha")
public class UserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}
