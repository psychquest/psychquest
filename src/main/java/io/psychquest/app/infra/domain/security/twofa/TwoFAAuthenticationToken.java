package io.psychquest.app.infra.domain.security.twofa;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * AuthenticationToken for Two Factor Authentication.
 */
public class TwoFAAuthenticationToken extends AbstractAuthenticationToken {

    private String userLogin;
    private String token;

    /**
     * Creates a token with the supplied user login and verification code marked as not authenticated. Safe usage: everywhere.
     */
    public TwoFAAuthenticationToken(String userLogin, String verificationCode) {
        super(null);
        this.userLogin = userLogin;
        this.token = verificationCode;
        this.setAuthenticated(false);
    }

    /**
     * Creates a token with the supplied array of authorities. Sets the token to be authenticated. Safe usage ONLY(!!!)
     * within an AuthenticationProvider!
     */
    TwoFAAuthenticationToken(Collection<? extends GrantedAuthority> authorities, String userLogin, String verificationCode) {
        super(authorities);
        this.userLogin = userLogin;
        this.token = verificationCode;
        this.setAuthenticated(true);
    }

    @Override
    public String getCredentials() {
        return token;
    }

    @Override
    public String getPrincipal() {
        return userLogin;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TwoFAAuthenticationToken)) return false;
        if (!super.equals(o)) return false;

        TwoFAAuthenticationToken that = (TwoFAAuthenticationToken) o;

        if (!userLogin.equals(that.userLogin)) return false;
        return token.equals(that.token);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + userLogin.hashCode();
        result = 31 * result + token.hashCode();
        return result;
    }
}
