package io.psychquest.app.infra.domain.security.twofa;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.jboss.aerogear.security.otp.Totp;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import io.psychquest.app.infra.domain.security.DomainUserDetails;

@Component
public class TwoFAAuthenticationProvider implements AuthenticationProvider {

    private static final String INVALID_USERNAME_OR_VERIFICATION_CODE = "Invalid username or verification code";
    private final UserDetailsService userDetailsService;

    public TwoFAAuthenticationProvider(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Override
    public Authentication authenticate(Authentication auth) throws AuthenticationException {
        String userLogin = (String) auth.getPrincipal();
        if (StringUtils.isNotBlank(userLogin)) {
            DomainUserDetails user = (DomainUserDetails) userDetailsService.loadUserByUsername(userLogin);
            if (user != null && userHasValid2FAOptionsSet(user)) {
                String verificationCode = (String) auth.getCredentials();
                if (verify(verificationCode, user.getTwoFASecret())) {
                    return new TwoFAAuthenticationToken(user.getAuthorities(), userLogin, verificationCode);
                } else {
                    throw new BadCredentialsException("Invalid verification code!");
                }
            } else {
                throw new BadCredentialsException(INVALID_USERNAME_OR_VERIFICATION_CODE);
            }
        } else {
            throw new BadCredentialsException(INVALID_USERNAME_OR_VERIFICATION_CODE);
        }
    }

    private boolean verify(String verificationToken, String userSecret) {
        Totp totp = new Totp(userSecret);
        return NumberUtils.isCreatable(verificationToken) && totp.verify(verificationToken);
    }

    private boolean userHasValid2FAOptionsSet(DomainUserDetails user) {
        return user.getUses2FA() == Boolean.TRUE
            && StringUtils.isNotBlank(user.getTwoFASecret());
    }

    private boolean additionalVerification(DomainUserDetails user) {
        return user.isAccountNonExpired() && user.isAccountNonLocked() && user.isCredentialsNonExpired() && user.isEnabled();
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(TwoFAAuthenticationToken.class);
    }
}
