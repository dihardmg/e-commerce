package com.ecommerce.security.util;

import lombok.experimental.UtilityClass;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Security Utility Class
 */
@UtilityClass
public class SecurityUtils {

    /**
     * Get current authenticated username
     */
    public Optional<String> getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return Optional.of(authentication.getName());
        }
        return Optional.empty();
    }

    /**
     * Get current authentication
     */
    public Optional<Authentication> getCurrentAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return Optional.of(authentication);
        }
        return Optional.empty();
    }

    /**
     * Get current user authorities
     */
    public List<String> getCurrentUserAuthorities() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getAuthorities() != null) {
            return authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());
        }
        return List.of();
    }

    /**
     * Check if current user has specific authority
     */
    public boolean hasAuthority(String authority) {
        return getCurrentUserAuthorities().contains(authority);
    }

    /**
     * Check if current user has any of the specified authorities
     */
    public boolean hasAnyAuthority(String... authorities) {
        List<String> userAuthorities = getCurrentUserAuthorities();
        return List.of(authorities).stream()
                .anyMatch(userAuthorities::contains);
    }

    /**
     * Check if current user has all of the specified authorities
     */
    public boolean hasAllAuthorities(String... authorities) {
        List<String> userAuthorities = getCurrentUserAuthorities();
        return List.of(authorities).stream()
                .allMatch(userAuthorities::contains);
    }

    /**
     * Check if current user has specific role
     */
    public boolean hasRole(String role) {
        String roleWithPrefix = role.startsWith("ROLE_") ? role : "ROLE_" + role;
        return hasAuthority(roleWithPrefix);
    }

    /**
     * Check if current user has any of the specified roles
     */
    public boolean hasAnyRole(String... roles) {
        String[] rolesWithPrefix = new String[roles.length];
        for (int i = 0; i < roles.length; i++) {
            rolesWithPrefix[i] = roles[i].startsWith("ROLE_") ? roles[i] : "ROLE_" + roles[i];
        }
        return hasAnyAuthority(rolesWithPrefix);
    }

    /**
     * Check if current user is admin
     */
    public boolean isAdmin() {
        return hasRole("ADMIN");
    }

    /**
     * Check if current user is authenticated
     */
    public boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated() &&
               !"anonymousUser".equals(authentication.getName());
    }

    /**
     * Get authorities as strings from authentication
     */
    public List<String> getAuthoritiesFromAuthentication(Authentication authentication) {
        if (authentication != null && authentication.getAuthorities() != null) {
            return authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());
        }
        return List.of();
    }

    /**
     * Check if authentication has specific authority
     */
    public boolean authenticationHasAuthority(Authentication authentication, String authority) {
        return getAuthoritiesFromAuthentication(authentication).contains(authority);
    }

    /**
     * Check if authentication has specific role
     */
    public boolean authenticationHasRole(Authentication authentication, String role) {
        String roleWithPrefix = role.startsWith("ROLE_") ? role : "ROLE_" + role;
        return authenticationHasAuthority(authentication, roleWithPrefix);
    }

    /**
     * Extract user ID from authentication if available
     */
    public Optional<String> getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            // Try to get user ID from authentication details or principal
            Object details = authentication.getDetails();
            if (details instanceof String) {
                return Optional.of((String) details);
            }

            // Fall back to username if no specific user ID is available
            return Optional.of(authentication.getName());
        }
        return Optional.empty();
    }
}