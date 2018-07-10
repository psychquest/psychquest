package io.psychquest.app.infra.domain.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

/**
 * Spring Security's UserDetails with extended options for 2FA auth.
 */
public class DomainUserDetails extends User {

    private Boolean uses2FA;
    private String twoFASecret;

    DomainUserDetails(String username, String password, boolean activated, boolean locked, Collection<? extends GrantedAuthority> authorities, Boolean uses2FA, String twoFASecret) {
        super(username, password, activated, !locked, !locked, !locked, authorities);
        this.uses2FA = uses2FA;
        this.twoFASecret = twoFASecret;
    }

    public Boolean getUses2FA() {
        return uses2FA;
    }

    public String getTwoFASecret() {
        return twoFASecret;
    }

    @Override
    public void eraseCredentials() {
        super.eraseCredentials();
        this.twoFASecret = null;
    }

}
