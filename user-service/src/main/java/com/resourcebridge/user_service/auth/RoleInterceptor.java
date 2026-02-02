package com.resourcebridge.user_service.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;
import java.util.Set;

@Slf4j
@Component
public class RoleInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler)
            throws Exception {

        if (!(handler instanceof HandlerMethod method)) {
            return true;
        }

        RoleAllowed allowed =
                method.getMethodAnnotation(RoleAllowed.class);

        if (allowed == null) {
            allowed =
                    method.getBeanType()
                            .getAnnotation(RoleAllowed.class);
        }

        if (allowed == null) {
            return true; // no restriction
        }

        String userRole =
                UserContextHolder.getCurrentRole();

        if (userRole == null) {

            log.warn("No role in context");

            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }

        Set<String> allowedRoles =
                Set.of(allowed.value());

        if (!allowedRoles.contains(userRole)) {

            log.warn("Access denied | role={} | allowed={}",
                    userRole, allowedRoles);

            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }

        return true;
    }
}
