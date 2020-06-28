package com.interest.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "interest.gitee")
@Component
@Data
public class GiteeProperties {

    private String clientId;

    private String clientSecret;

    private String redirectUrl;

}
