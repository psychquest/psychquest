package io.psychquest.app.infra.domain.web.vm;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import lombok.Data;

/**
 * View Model object for storing a user's 2FA verification code.
 */
@Data
public class TwoFAVerificationVM {
    @NotBlank
    @Size(min = 6, max = 6)
    private String token;

    private boolean rememberMe;
}
