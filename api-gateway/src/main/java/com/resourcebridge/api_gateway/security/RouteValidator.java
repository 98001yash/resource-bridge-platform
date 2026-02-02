package com.resourcebridge.api_gateway.security;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
public class RouteValidator {

    /* ================= ROLE → ROUTE MAP ================= */

    private static final Map<String, String> ROLE_ROUTE_MAP =
            Map.of(
                    "ADMIN", "/admin",
                    "DONOR", "/donor",
                    "NGO", "/ngo",
                    "CLINIC", "/clinic",
                    "VOLUNTEER", "/volunteer"
            );

    // Roles that can access /user/**
    private static final Set<String> USER_SERVICE_ROLES =
            Set.of("DONOR", "NGO", "CLINIC", "VOLUNTEER", "ADMIN");

    public boolean isAuthorized(String role, String path) {

        if (role == null || path == null) {
            return false;
        }

        // Admin → full access
        if (role.equals("ADMIN")) {
            return true;
        }

        // Everyone can access user-service
        if (path.startsWith("/user")) {
            return USER_SERVICE_ROLES.contains(role);
        }

        // Role-based routes
        String allowedPrefix = ROLE_ROUTE_MAP.get(role);

        if (allowedPrefix == null) {
            return false;
        }

        return path.startsWith(allowedPrefix);
    }
}
