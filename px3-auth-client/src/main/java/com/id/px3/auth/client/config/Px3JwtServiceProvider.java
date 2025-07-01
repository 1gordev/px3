package com.id.px3.auth.client.config;

import com.id.px3.rest.security.JwtService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Px3JwtServiceProvider {

    @Bean
    public JwtService jwtService() {
        return new JwtService();
    }
}
