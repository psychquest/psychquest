package io.psychquest.app.infra.domain;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import io.psychquest.app.infra.domain.dto.UserDTO;
import io.psychquest.app.infra.domain.model.AppLocale;
import io.psychquest.app.infra.domain.model.Authority;
import io.psychquest.app.infra.domain.model.AuthorityRepository;
import io.psychquest.app.infra.domain.model.User;
import io.psychquest.app.infra.domain.model.UserCredentials;
import io.psychquest.app.infra.domain.model.UserRepository;
import io.psychquest.app.infra.domain.security.AuthoritiesConstants;
import io.psychquest.app.infra.domain.security.SecurityUtils;
import io.psychquest.app.infra.domain.web.errors.InvalidPasswordException;
import io.psychquest.app.util.RandomUtil;

/**
 * Service class for managing users.
 */
@Service
@Transactional
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthorityRepository authorityRepository;


    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, AuthorityRepository authorityRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authorityRepository = authorityRepository;
    }

    public Optional<User> activateRegistration(String key) {
        log.debug("Activating user for activation key {}", key);
        return userRepository.findOneByCredentials_ActivationKey(key)
            .map(user -> {
                user.getCredentials().setActivated(true);
                user.getCredentials().setActivationKey(null);
                log.debug("Activated user: {}", user);
                return user;
            });
    }

    public Optional<User> completePasswordReset(String newPassword, String key) {
        log.debug("Reset user password for reset key {}", key);

        return userRepository.findOneByCredentials_PasswordResetKey(key)
            .filter(user -> user.getCredentials().getPasswordResetKeyCreatedAt().isAfter(Instant.now().minusSeconds(86400)))
            .map(user -> {
                user.getCredentials().setPasswordHash(passwordEncoder.encode(newPassword));
                user.getCredentials().setPasswordResetKey(null);
                user.getCredentials().setPasswordResetKeyCreatedAt(null);
                return user;
            });
    }

    public Optional<User> requestPasswordReset(String email) {
        return userRepository.findOneByEmailIgnoreCase(email)
            .filter(user -> user.getCredentials() != null && user.getCredentials().isActivated())
            .map(user -> {
                user.getCredentials().setPasswordResetKey(RandomUtil.generateResetKey());
                user.getCredentials().setPasswordResetKeyCreatedAt(Instant.now());
                return user;
            });
    }

    public User registerUser(UserDTO userDTO, String password) {
        User newUser = new User();
        String encryptedPassword = passwordEncoder.encode(password);
        newUser.setEmail(StringUtils.trim(userDTO.getEmail()));
        newUser.setName(StringUtils.trim(userDTO.getName()));
        UserCredentials credentials = new UserCredentials();
        credentials.setPasswordHash(encryptedPassword);
        credentials.setActivated(false);
        credentials.setActivationKey(RandomUtil.generateActivationKey());
        Optional<Authority> optionalAuthority = authorityRepository.findOneByName(AuthoritiesConstants.USER);
        optionalAuthority.ifPresent(a -> credentials.setAuthorities(Collections.singleton(a)));
        newUser.setCredentials(credentials);
        log.debug("Created Information for User: {}", newUser);
        return userRepository.save(newUser);
    }

    /**
     * Update basic information for the current user.
     *
     * @param name           name of user
     * @param email          email id of user
     * @param preferedLocale the prefered locale for that user
     */
    public void updateUser(String email, String name, AppLocale preferedLocale) {
        SecurityUtils.getCurrentUserLogin()
            .flatMap(userRepository::findOneByEmailIgnoreCase)
            .ifPresent(user -> {
                user.setEmail(StringUtils.trim(email));
                user.setName(StringUtils.trim(name));
                user.setPreferedLocale(preferedLocale);
                log.debug("Changed Information for User: {}", user);
            });
    }

    public void changePassword(String currentClearTextPassword, String newPassword) {
        SecurityUtils.getCurrentUserLogin()
            .flatMap(userRepository::findOneByEmailIgnoreCase)
            .ifPresent(user -> {
                String currentEncryptedPassword = user.getCredentials().getPasswordHash();
                if (!passwordEncoder.matches(currentClearTextPassword, currentEncryptedPassword)) {
                    throw new InvalidPasswordException();
                }
                String encryptedPassword = passwordEncoder.encode(newPassword);
                user.getCredentials().setPasswordHash(encryptedPassword);
                log.debug("Changed password for User: {}", user);
            });
    }

    /**
     * @param email the user's email
     * @return the user of that email, queried case insensitive
     */
    @Transactional(readOnly = true)
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findOneByEmailIgnoreCase(email);
    }

    /**
     * @return the currently logged in user
     */
    @Transactional(readOnly = true)
    public Optional<User> getCurrentUser() {
        return SecurityUtils.getCurrentUserLogin().flatMap(userRepository::findOneByEmailIgnoreCase);
    }

    /**
     * Not activated users should be automatically deleted after 3 days.
     * <p>
     * This is scheduled to get fired everyday, at 01:00 (am).
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void removeNotActivatedUsers() {
        log.info("User housekeeping job started.");
        long start = System.currentTimeMillis();
        Set<User> users = userRepository.findAllByCredentials_ActivatedIsFalseAndCredentials_ActivationKeyCreatedAtBefore(Instant.now().minus(3, ChronoUnit.DAYS));
        for (User user : users) {
            log.debug("Deleting not activated user {}", user.getEmail());
            userRepository.delete(user);
        }
        log.info("User housekeeping job finished after {}ms", System.currentTimeMillis() - start);
    }

}
