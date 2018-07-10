package io.psychquest.app.infra.domain.model;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthorityRepository extends Neo4jRepository<Authority, Long> {
    Optional<Authority> findOneByName(String name);
}
