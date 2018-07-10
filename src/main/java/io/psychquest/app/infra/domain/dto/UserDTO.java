package io.psychquest.app.infra.domain.dto;

import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import io.psychquest.app.infra.domain.model.AppLocale;
import io.psychquest.app.infra.domain.model.Authority;
import io.psychquest.app.infra.domain.model.User;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class UserDTO {

    private Long id;
    @NotNull
    @Size(max = 254)
    @Email
    private String email;
    @Size(max = 250)
    private String name;
    private Set<String> authorities;
    private AppLocale preferedLocale;

    public UserDTO(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.name = user.getName();
        if (user.getCredentials() != null && user.getCredentials().getAuthorities() != null) {
            authorities = user.getCredentials().getAuthorities().stream().map(Authority::getName).collect(Collectors.toSet());
        }
        this.preferedLocale = user.getPreferedLocale();
    }
}
