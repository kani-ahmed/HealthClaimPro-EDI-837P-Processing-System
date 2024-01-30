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
