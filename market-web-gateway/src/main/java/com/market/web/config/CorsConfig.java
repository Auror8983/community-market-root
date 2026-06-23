package com.market.web.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        // 1. 允许任何来源
        config.addAllowedOriginPattern("*");
        // 2. 允许任何头
        config.addAllowedHeader("*");
        // 3. 允许任何方法 (GET, POST, PUT, DELETE, OPTIONS)
        config.addAllowedMethod("*");
        // 4. 允许携带凭证
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // 对所有路径应用该配置
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}