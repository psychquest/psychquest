package io.psychquest.app.infra.domain.model;

import org.apache.commons.lang3.StringUtils;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import java.util.Set;

import javax.validation.constraints.NotNull;

import io.psychquest.app.domain.GraphRelationTypes;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NodeEntity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "email")
public class User {

    @Id
    @GeneratedValue
    private Long id;
    @NotNull
    private String email;
    private String name;
    @Relationship(type = GraphRelationTypes.OWNS)
    private UserCredentials credentials;
    @Relationship(type = GraphRelationTypes.PREFERS)
    private AppLocale preferedLocale;
    @Relationship(type = GraphRelationTypes.IS_MEMBER_OF)
    private Set<Organization> organizations;

    @Override
    public String toString() {
        return "User{" +
            "id=" + id +
            ", email='" + StringUtils.abbreviate(email, 10) + '\'' +
            ", name='" + name + '\'' +
            ", preferedLocale=" + preferedLocale +
            ", organizations=" + organizations +
            '}';
    }
}
