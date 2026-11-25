package com.example.IRCTC.config;

import feign.RequestInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.beans.factory.annotation.Value;

@Configuration
public class FeignAuthConfig {
    @Value("${APP_INTERNAL_SERVICE_TOKEN:local-internal-token-change-me}")
    private String internalServiceToken;

    @Bean
    public RequestInterceptor authorizationHeaderForwarder() {
        return template -> {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

            if (attributes == null) {
                return;
            }

            HttpServletRequest request = attributes.getRequest();
            String authorization = request.getHeader("Authorization");
            if (authorization != null && !authorization.isBlank()) {
                template.header("Authorization", authorization);
            }
            template.header("X-Internal-Service-Token", internalServiceToken);
        };
    }
}
