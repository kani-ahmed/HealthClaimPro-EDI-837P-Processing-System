package com.billing.webapp;

import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/*
    * This is the main configuration file for the application
    * It is used to configure the application
    * The @Configuration annotation is used to indicate that this is a configuration file
    * The @Bean annotation is used to indicate that the method is used to configure a bean
    * The Hibernate5Module is used to configure the Hibernate5Module
    * It is used to serialize Hibernate entities to JSON
    * It is used to avoid:
    *  The LazyInitializationException, Jackson serialization error, Jackson deserialization error, Jackson infinite recursion error, Jackson infinite loop error and so on.
 */
@Configuration
public class AppConfig {

    @Bean
    public Hibernate5Module hibernate5Module() {
        Hibernate5Module module = new Hibernate5Module();
        // Configure the Hibernate5Module to serialize Hibernate entities to JSON
        return module;
    }

    // Other @Bean definitions can go here
}
