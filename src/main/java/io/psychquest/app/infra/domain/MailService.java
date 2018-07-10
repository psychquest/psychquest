package io.psychquest.app.infra.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import io.psychquest.app.infra.domain.model.User;

@Service
public class MailService {
    private static final Logger log = LoggerFactory.getLogger(MailService.class);

    public void sendActivationEmail(User user) {
        log.debug("Sending activation email to user {}", user);
    }

    public void sendPasswordResetMail(User user) {
        log.debug("Sending password reset mail to user {}", user);
    }
}
