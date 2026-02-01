package com.resourcebridge.api_gateway.security;

import org.springframework.stereotype.Component;

import java.util.Map;

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

    public boolean isAuthorized(String role, String path) {

        if (role == null || path == null) {
            return false;
        }

        // Admin can access everything
        if (role.equals("ADMIN")) {
            return true;
        }

        String allowedPrefix = ROLE_ROUTE_MAP.get(role);

        if (allowedPrefix == null) {
            return false;
        }

        return path.startsWith(allowedPrefix);
    }
}
