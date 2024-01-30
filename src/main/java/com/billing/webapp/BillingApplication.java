package com.billing.webapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/*
    * This is the main class for the application which runs the application
    * The @SpringBootApplication annotation is used to indicate that this is the main class and the main entry point for the application
 */

@SpringBootApplication
public class BillingApplication {
    public static void main(String[] args) {
        SpringApplication.run(BillingApplication.class, args);
    }
}
