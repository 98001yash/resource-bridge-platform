package com.resourcebridge.user_service.auth;


import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.stereotype.Component;

@Component
public class FeignClientInterceptor
        implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {

        Long userId =
                UserContextHolder.getCurrentUserId();

        String role =
                UserContextHolder.getCurrentRole();

        Boolean verified =
                UserContextHolder.isVerified();

        if (userId != null) {

            template.header(
                    AuthConstants.USER_ID,
                    userId.toString()
            );
        }

        if (role != null) {

            template.header(
                    AuthConstants.ROLE,
                    role
            );
        }

        if (verified != null) {

            template.header(
                    AuthConstants.VERIFIED,
                    verified.toString()
            );
        }
    }
}
