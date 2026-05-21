package com.e_health_care.web; // Kiểm tra lại package

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/img/avatars/**")
                .addResourceLocations("file:./DoAnThucTeCNPM/web/src/main/resources/static/img/avatars/");
    }
}