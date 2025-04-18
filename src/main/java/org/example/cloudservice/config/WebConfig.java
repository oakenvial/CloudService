package org.example.cloudservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Data
@Configuration
@ConfigurationProperties(prefix = "app.cors")
public class WebConfig implements WebMvcConfigurer {

    private String[] allowedOrigins;
    private String[] allowedMethods;
    private String[] allowedHeaders;
    private String[] exposedHeaders;
    private long maxAge = 3600;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowCredentials(true)
                .allowedOriginPatterns(allowedOrigins)
                .allowedMethods(allowedMethods)
                .allowedHeaders(allowedHeaders)
                .exposedHeaders(exposedHeaders)
                .maxAge(maxAge);
    }
}
