package com.id.px3.auth.client.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class Px3AuthClientAppConfig {

    @Value("${px3.auth.base-url:}")
    private String px3AuthBaseUrl;

    @Value("${px3.user.base-url:}")
    private String px3UserBaseUrl;
}
