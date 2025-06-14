package com.id.px3.auth.client.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class AppConfig {

    @Value("${px3.auth.base-url}")
    private String px3AuthBaseUrl;

}
