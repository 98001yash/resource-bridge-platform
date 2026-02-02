package com.resourcebridge.user_service.auth;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final UserInterceptor userInterceptor;
    private final RoleInterceptor roleInterceptor;

    public WebConfig(UserInterceptor userInterceptor,
                     RoleInterceptor roleInterceptor) {

        this.userInterceptor = userInterceptor;
        this.roleInterceptor = roleInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        registry.addInterceptor(userInterceptor)
                .order(1);

        registry.addInterceptor(roleInterceptor)
                .order(2);
    }
}
