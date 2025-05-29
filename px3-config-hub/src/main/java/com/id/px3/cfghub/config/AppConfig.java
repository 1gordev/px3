package com.id.px3.cfghub.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class AppConfig {

    @Value("${px3.config.hub.collection:ConfigHub}")
    private String configHubCollection;

}
