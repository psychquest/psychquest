package io.psychquest.app.infra.domain.model;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import java.time.Instant;
import java.util.Set;

import javax.validation.constraints.NotNull;

import io.psychquest.app.domain.GraphRelationTypes;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NodeEntity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserCredentials {

    @Id
    @GeneratedValue
    private Long id;
    @NotNull
    private String passwordHash;
    private boolean locked;
    private boolean activated;
    private Boolean uses2FA;
    private String twoFASecretHash;
    @Relationship(type = GraphRelationTypes.IS_ASSIGNED_TO)
    private Set<Authority> authorities;
    private int failedLoginAttempts;
    private Instant loginSuccessAt;
    private String activationKey;
    private Instant activationKeyCreatedAt;
    private String passwordResetKey;
    private Instant passwordResetKeyCreatedAt;

    @Override
    public String toString() {
        return "UserCredentials{" +
            "id=" + id +
            ", passwordHash='" + (passwordHash != null ? "********" : "") + '\'' +
            ", uses2FA=" + uses2FA +
            ", locked=" + locked +
            ", activated=" + activated +
            ", totpSecretHash='" + (twoFASecretHash != null ? "********" : "") + '\'' +
            ", authorities=" + authorities +
            ", failedLoginAttempts=" + failedLoginAttempts +
            ", loginSuccessAt=" + loginSuccessAt +
            ", activationKey='" + activationKey + '\'' +
            ", activationKeyCreatedAt=" + activationKeyCreatedAt +
            ", passwordResetKey='" + passwordResetKey + '\'' +
            ", passwordResetKeyCreatedAt=" + passwordResetKeyCreatedAt +
            '}';
    }
}
