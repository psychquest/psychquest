package io.psychquest.app.infra.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;

import java.nio.file.Paths;

@Configuration
@EnableNeo4jRepositories(basePackages = "io.psychquest.app.infra.domain.model")
@EntityScan(basePackages = "io.psychquest.app.infra.domain.model")
public class DatabaseConfiguration {


    @Profile("dev")
    @Bean
    public org.neo4j.ogm.config.Configuration configuration() {
        String embeddedDBUri = Paths.get("target/neo4j.db").toAbsolutePath().toString();
        return new org.neo4j.ogm.config.Configuration.Builder()
            .uri("file://" + embeddedDBUri)
            .build();
    }
}
