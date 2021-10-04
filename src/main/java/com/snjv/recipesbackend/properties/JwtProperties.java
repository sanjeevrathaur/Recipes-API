package com.snjv.recipesbackend.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
@ConfigurationProperties("app.jwt")
public class JwtProperties {

    private String secret;

    private int expirationMs;

}
