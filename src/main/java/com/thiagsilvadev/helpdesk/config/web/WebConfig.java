package com.thiagsilvadev.helpdesk.config.web;

import com.thiagsilvadev.helpdesk.security.web.CurrentUserIdResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final CurrentUserIdResolver currentUserIdResolver;

    public WebConfig(CurrentUserIdResolver currentUserIdResolver) {
        this.currentUserIdResolver = currentUserIdResolver;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(currentUserIdResolver);
    }
}
