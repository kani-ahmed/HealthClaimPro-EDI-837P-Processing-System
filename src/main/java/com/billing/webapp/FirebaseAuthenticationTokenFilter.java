package com.billing.webapp;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Collections;

public class FirebaseAuthenticationTokenFilter extends OncePerRequestFilter {
    /*
    * This filter is responsible for authenticating the user based on the Firebase token.
    * It is executed for every request. It checks if the Authorization header is present and if it is, it verifies the token.
    * If the token is valid, it sets the authentication object in the SecurityContext.
    * If the token is invalid, it returns a 401 Unauthorized response.
     */

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        // Bypass authentication for local requests (development only)
        String requestIP = request.getRemoteAddr();
        if ("127.0.0.1".equals(requestIP) || "0:0:0:0:0:0:0:1".equals(requestIP)) {
            // Log a warning or info to remind you that authentication is being bypassed
            System.out.println("Bypassing authentication for request from localhost for development purposes.");

            // Set a fully authenticated user for the SecurityContext
            Authentication authentication = new UsernamePasswordAuthenticationToken("localUser", null, Collections.emptyList());
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Continue the filter chain with the authenticated user
            filterChain.doFilter(request, response);
            return; // Important to return here so the rest of the method is skipped
        }

        String authToken = request.getHeader("Authorization");

        try {
            // Check if the Authorization header is present and if it is, verify the token
            if (authToken != null && ! authToken.isEmpty() && authToken.startsWith("Bearer ")) {
                authToken = authToken.substring(7); // Remove "Bearer " prefix
                // Verify the token and get the user ID from it (the user ID is the Firebase UID)
                FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(authToken);
                // Create an authentication object and set it in the SecurityContext to authenticate the user
                String uid = decodedToken.getUid();
                // The authentication object is a UsernamePasswordAuthenticationToken with the user ID as the principal and an empty list of authorities
                // The authorities are not used in this application but they are required by Spring Security so we pass an empty list of authorities here
                Authentication authentication = new UsernamePasswordAuthenticationToken(uid, authToken, Collections.emptyList());
                // Set the authentication object in the SecurityContext to authenticate the user for this request
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            SecurityContextHolder.clearContext();
            return;
        }
        filterChain.doFilter(request, response);
    }
}
