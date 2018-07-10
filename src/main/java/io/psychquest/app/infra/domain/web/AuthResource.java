package io.psychquest.app.infra.domain.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import io.psychquest.app.infra.domain.security.AuthoritiesConstants;
import io.psychquest.app.infra.domain.security.SecurityUtils;
import io.psychquest.app.infra.domain.security.jwt.TokenProvider;
import io.psychquest.app.infra.domain.security.twofa.TwoFAAuthenticationToken;
import io.psychquest.app.infra.domain.web.vm.JwtVM;
import io.psychquest.app.infra.domain.web.vm.LoginVM;
import io.psychquest.app.infra.domain.web.vm.TwoFAVerificationVM;
import io.psychquest.app.util.HttpHeaders;

/**
 * Controller to authenticate users.
 */
@RestController
@RequestMapping("/api")
public class AuthResource {

    private final TokenProvider tokenProvider;

    private final AuthenticationManager authenticationManager;

    public AuthResource(TokenProvider tokenProvider, AuthenticationManager authenticationManager) {
        this.tokenProvider = tokenProvider;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/authenticate")
    public ResponseEntity<JwtVM> authorize(@Valid @RequestBody LoginVM loginVM) {
        UsernamePasswordAuthenticationToken authenticationToken =
            new UsernamePasswordAuthenticationToken(loginVM.getEmail(), loginVM.getPassword());
        Authentication authentication = this.authenticationManager.authenticate(authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.createToken(authentication, loginVM.isRememberMe());
        JwtVM jwtVM = new JwtVM(jwt);
        boolean isUsing2FA = authentication.getAuthorities().stream().anyMatch(authority -> AuthoritiesConstants.PRE_AUTH_USER.equals(authority.getAuthority()));
        if (isUsing2FA) {
            return ResponseEntity.status(HttpStatus.CREATED).headers(HttpHeaders.authorization(jwt).build()).body(jwtVM);
        } else {
            return ResponseEntity.ok().headers(HttpHeaders.authorization(jwt).build()).body(jwtVM);
        }
    }

    @Secured({AuthoritiesConstants.PRE_AUTH_USER})
    @PostMapping("/authenticate/2fa")
    public ResponseEntity<JwtVM> authorize2FA(@Valid @RequestBody TwoFAVerificationVM twoFAVerificationVM) {
        return SecurityUtils.getCurrentUserLogin().map(userLogin -> {
            TwoFAAuthenticationToken authenticationToken = new TwoFAAuthenticationToken(userLogin, twoFAVerificationVM.getToken());
            Authentication authentication = this.authenticationManager.authenticate(authenticationToken);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = tokenProvider.createToken(authentication, twoFAVerificationVM.isRememberMe());
            return ResponseEntity.ok().headers(HttpHeaders.authorization(jwt).build()).body(new JwtVM(jwt));
        }).orElseThrow(() -> new BadCredentialsException("User must authenticate before entering 2FA stage!"));
    }
}
