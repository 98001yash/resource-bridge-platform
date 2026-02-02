package com.resourcebridge.user_service.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
public class UserInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {

        String userId =
                request.getHeader(AuthConstants.USER_ID);

        String role =
                request.getHeader(AuthConstants.ROLE);

        String verified =
                request.getHeader(AuthConstants.VERIFIED);

        if (userId == null || role == null) {

            log.warn("Missing auth headers");

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        UserContextHolder.setUserId(Long.parseLong(userId));
        UserContextHolder.setRole(role);

        if (verified != null) {
            UserContextHolder.setVerified(
                    Boolean.parseBoolean(verified)
            );
        }

        log.debug("Context set | id={} | role={}",
                userId, role);

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler,
                                Exception ex) {

        UserContextHolder.clear();
    }
}
