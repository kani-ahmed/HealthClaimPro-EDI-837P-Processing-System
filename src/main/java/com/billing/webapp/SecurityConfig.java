package com.billing.webapp;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    /*
     * This is the security configuration for the application.
     * It is used to configure the security filters and the security chain.
     * The security chain is used to determine which endpoints are secured and which are not.
     * The filter is used to authenticate the user.
     * The filter is added before the UsernamePasswordAuthenticationFilter.
     * The filter is used to authenticate the user using the Firebase token.
     * The filter is executed for every request.
     * The filter checks if the Authorization header is present and if it is, it verifies the token.
     * If the token is valid, it sets the authentication object in the SecurityContext.
     * If the token is invalid, it returns a 401 Unauthorized response.
     */

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF protection for this application because it is not needed for this application, and it is not recommended to use CSRF protection for REST APIs
                .csrf(csrf -> csrf.disable())
                // Configure the security chain to secure the /api/** endpoints and permit all other requests (the other requests are the requests for the frontend) without authentication
                .authorizeHttpRequests(auth -> auth
                        // Secure the /api/** endpoints (the endpoints for the REST API)
                        .requestMatchers("/api/**").authenticated()
                        // Permit all other requests (the requests for the frontend) [for static resources, index.html, favicon.ico, ...]
                        .anyRequest().permitAll()
                )
                // Configure the session management to use stateless sessions (the application does not use sessions) to avoid creating unnecessary sessions for every request
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Add the Firebase authentication filter before the UsernamePasswordAuthenticationFilter to authenticate the user using the Firebase token
                .addFilterBefore(new FirebaseAuthenticationTokenFilter(), UsernamePasswordAuthenticationFilter.class);

        // Build the security chain and return it to be used by Spring Security to secure the application endpoints and authenticate the user using the Firebase token
        return http.build();
    }
}
