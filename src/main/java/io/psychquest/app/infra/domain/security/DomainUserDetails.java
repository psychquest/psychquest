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


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DomainUserDetails)) return false;
        if (!super.equals(o)) return false;

        DomainUserDetails that = (DomainUserDetails) o;

        if (uses2FA != null ? !uses2FA.equals(that.uses2FA) : that.uses2FA != null) return false;
        return twoFASecret != null ? twoFASecret.equals(that.twoFASecret) : that.twoFASecret == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (uses2FA != null ? uses2FA.hashCode() : 0);
        result = 31 * result + (twoFASecret != null ? twoFASecret.hashCode() : 0);
        return result;
    }
}
