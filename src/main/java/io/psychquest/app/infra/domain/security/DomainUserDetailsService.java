package io.psychquest.app.infra.domain.security;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.internal.constraintvalidators.hv.EmailValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import io.psychquest.app.infra.domain.model.User;
import io.psychquest.app.infra.domain.model.UserRepository;

/**
 * Authenticate a user from the database.
 */
@Component("userDetailsService")
public class DomainUserDetailsService implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(DomainUserDetailsService.class);

    private final UserRepository userRepository;

    public DomainUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) {
        log.debug("Authenticating user {}", email);
        String lowerCaseEmail = StringUtils.lowerCase(email, Locale.GERMAN);
        boolean isvalidEmail = new EmailValidator().isValid(lowerCaseEmail, null);
        if (isvalidEmail) {
            User user = this.userRepository.findOneByEmailIgnoreCase(email)
                .orElseThrow(() -> new UsernameNotFoundException("User with email " + email + " was not found in the database"));
            return createSpringSecurityUser(email, user);
        } else {
            throw new UsernameNotFoundException("User with email " + email + " was not found in the database");
        }
    }

    private DomainUserDetails createSpringSecurityUser(String lowercaseLogin, User user) {
        if (!user.getCredentials().isActivated()) {
            throw new UserNotActivatedException("User " + lowercaseLogin + " was not activated");
        } else {
            List<GrantedAuthority> grantedAuthorities;
            if (user.getCredentials().getUses2FA() == Boolean.TRUE) {
                grantedAuthorities = Collections.singletonList(new SimpleGrantedAuthority(AuthoritiesConstants.PRE_AUTH_USER));
            } else {
                grantedAuthorities = user.getCredentials().getAuthorities().stream()
                    .map(authority -> new SimpleGrantedAuthority(authority.getName()))
                    .collect(Collectors.toList());
            }

            return new DomainUserDetails(
                user.getEmail(),
                user.getCredentials().getPasswordHash(),
                user.getCredentials().isActivated(),
                user.getCredentials().isLocked(),
                grantedAuthorities, user.getCredentials().getUses2FA(), user.getCredentials().getTwoFASecretHash()
            );
        }

    }
}
