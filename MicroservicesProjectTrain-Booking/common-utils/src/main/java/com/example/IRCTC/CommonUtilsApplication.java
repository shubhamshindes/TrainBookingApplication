package com.example.IRCTC;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Dummy entry point for the common-utils module.
 * This class satisfies the Spring Boot Maven plugin's requirement for a main class
 * when packaging the module as an executable jar. It does not expose any REST
 * endpoints and is not used at runtime by other services; it merely enables the
 * build to succeed.
 */
@SpringBootApplication
public class CommonUtilsApplication {
    public static void main(String[] args) {
        SpringApplication.run(CommonUtilsApplication.class, args);
    }
}
