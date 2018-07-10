package io.psychquest.app.infra.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;

@Configuration
@EnableNeo4jRepositories(basePackages = "io.psychquest.app.infra.domain.model")
@EntityScan(basePackages = "io.psychquest.app.infra.domain.model")
public class DatabaseConfiguration {

}
