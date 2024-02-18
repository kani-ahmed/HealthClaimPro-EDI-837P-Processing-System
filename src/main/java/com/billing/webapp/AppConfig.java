package com.billing.webapp;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.fasterxml.jackson.datatype.hibernate6.Hibernate6Module;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

/**
 * This is the configuration class for the application
 * This class is part of the configuration for a Spring Boot application, specifically tailored to integrate Jackson with Hibernate 6
 * for JSON serialization and deserialization. It ensures that JSON handling is compatible with Hibernate's lazy loading
 * and proxy objects, preventing issues like uninitialized lazy collections or proxies being serialized,
 * and handles null values and date formatting as per the application's requirements.
 */
@Configuration
public class AppConfig {

    @Bean
    public Jackson2ObjectMapperBuilder jacksonBuilder() {
        // Create a new Jackson2ObjectMapperBuilder instance
        Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
        // Configure the builder to not fail on unknown properties during deserialization
        builder.failOnUnknownProperties(false);
        // Exclude null values from serialization to JSON
        builder.serializationInclusion(JsonInclude.Include.NON_NULL);
        // Disable writing date and time values as timestamps
        builder.featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        // Set up filters, ignoring unknown filter IDs (useful when filters are used dynamically)
        builder.filters(new SimpleFilterProvider().setFailOnUnknownId(false));

        // Initialize and configure Hibernate6Module to support proper serialization of Hibernate proxies and lazy-loaded entities
        Hibernate6Module hibernate6Module = new Hibernate6Module();
        // Customize the module as per your requirements, for example:
        hibernate6Module.disable(Hibernate6Module.Feature.USE_TRANSIENT_ANNOTATION);

        // Add the configured Hibernate6Module to the Jackson2ObjectMapperBuilder
        builder.modules(hibernate6Module);

        // Return the configured builder
        return builder;
    }

    // Defines a bean for ObjectMapper using the configured Jackson2ObjectMapperBuilder
    @Bean
    public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
        // Build and return the ObjectMapper instance from the builder
        return builder.build();
    }

    // Other @Bean definitions can go here
}
