package io.psychquest.app.infra.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import javax.servlet.ServletContext;

/**
 * Configuration of web application with Servlet 3.0 APIs.
 */
@Configuration
public class WebAppConfiguration implements ServletContextInitializer {

    private static final Logger log = LoggerFactory.getLogger(WebAppConfiguration.class);

    private final Environment env;
    private final CorsConfiguration corsConfiguration;

    public WebAppConfiguration(Environment env, ApplicationProperties properties) {
        this.env = env;
        corsConfiguration = properties.getCors();
    }

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        if (corsConfiguration.getAllowedOrigins() != null && !corsConfiguration.getAllowedOrigins().isEmpty()) {
            log.debug("Registering CORS filter");
            source.registerCorsConfiguration("/api/**", corsConfiguration);
        }
        return new CorsFilter(source);
    }

    @Override
    public void onStartup(ServletContext servletContext) {
        if (env.getActiveProfiles().length != 0) {
            log.info("Web application configuration, using profiles: {}", (Object[]) env.getActiveProfiles());
        }
        if (env.acceptsProfiles(ApplicationConstants.SPRING_PROFILE_PRODUCTION)) {
            log.info("Initializing production features.");
        }
        if (env.acceptsProfiles(ApplicationConstants.SPRING_PROFILE_DEVELOPMENT)) {
            log.info("Initializing development features.");
        }
        log.info("Web application fully configured");
    }
}
