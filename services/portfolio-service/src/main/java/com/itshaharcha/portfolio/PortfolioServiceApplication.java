package com.itshaharcha.portfolio;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@ConfigurationPropertiesScan
// Scan com.itshaharcha so shared components (e.g. the common-lib exception handler) register.
@SpringBootApplication(scanBasePackages = "com.itshaharcha")
public class PortfolioServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PortfolioServiceApplication.class, args);
    }
}
