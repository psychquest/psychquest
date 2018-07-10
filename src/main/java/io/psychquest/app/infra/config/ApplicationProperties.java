package io.psychquest.app.infra.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.cors.CorsConfiguration;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import lombok.Getter;
import lombok.Setter;

@ConfigurationProperties(prefix = "psychquest", ignoreUnknownFields = false)
@Validated
@Getter
@Setter
public class ApplicationProperties {

    @NotNull
    @Pattern(regexp = ".[\\w'-]{30,}")
    private String passwordHashingSecret;

    @NotBlank
    private String jwtSigningSecret;

    @NotNull
    private Integer tokenValidityInSeconds;

    @NotNull
    private Integer tokenValidityInSecondsForRememberMe;

    private CorsConfiguration cors = new CorsConfiguration();

}
