package io.psychquest.app.infra.domain.security.jwt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.psychquest.app.infra.config.ApplicationProperties;

@Component
public class TokenProvider {

    private static final Logger log = LoggerFactory.getLogger(TokenProvider.class);
    private static final String AUTHORITIES_KEY = "auth";
    private final String secretKey;
    private final Integer tokenValidityInSeconds;
    private final Integer tokenValidityInSecondsForRememberMe;


    public TokenProvider(ApplicationProperties properties) {
        Base64.Encoder encoder = Base64.getEncoder();
        this.secretKey = encoder.encodeToString(properties.getJwtSigningSecret().getBytes(StandardCharsets.UTF_8));
        tokenValidityInSeconds = properties.getTokenValidityInSeconds();
        tokenValidityInSecondsForRememberMe = properties.getTokenValidityInSecondsForRememberMe();
    }

    /**
     * Create a JWT for the given authentication and the applicable expiration.
     *
     * @param authentication Spring Security Authentication
     * @param rememberMe     if <code>true</code>, then the extended expiration time is used
     * @return the Base64 encoded JWT
     */
    public String createToken(Authentication authentication, boolean rememberMe) {
        String authorities = authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.joining(","));

        Instant validity;
        if (rememberMe) {
            validity = Instant.now().plusSeconds(tokenValidityInSecondsForRememberMe);
        } else {
            validity = Instant.now().plusSeconds(tokenValidityInSeconds);
        }

        return Jwts.builder()
            .setSubject(authentication.getName())
            .claim(AUTHORITIES_KEY, authorities)
            .signWith(SignatureAlgorithm.HS512, secretKey)
            .setExpiration(Date.from(validity))
            .compact();
    }

    /**
     * Get Spring Securities {@link Authentication} from JWT token.
     *
     * @param token the JWT
     * @return the Spring Security Authentication, containing the username and granted authorities
     */
    Authentication getAuthentication(String token) {
        Claims claims = Jwts.parser()
            .setSigningKey(secretKey)
            .parseClaimsJws(token)
            .getBody();
        Collection<? extends GrantedAuthority> authorities =
            Arrays.stream(claims.get(AUTHORITIES_KEY).toString().split(","))
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
        User principal = new User(claims.getSubject(), "", authorities);
        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }

    /**
     * Validate a JWT.
     *
     * @param authToken the JWT
     * @return <code>true</code> if the token is valid - otherwise <code>false</code>
     */
    boolean validateToken(String authToken) {
        try {
            Jwts.parser().setSigningKey(secretKey).parseClaimsJws(authToken);
            return true;
        } catch (SignatureException e) {
            log.info("Invalid JWT signature.");
            log.trace("Invalid JWT signature trace: {}", e);
        } catch (MalformedJwtException e) {
            log.info("Invalid JWT token.");
            log.trace("Invalid JWT token trace: {}", e);
        } catch (ExpiredJwtException e) {
            log.info("Expired JWT token.");
            log.trace("Expired JWT token trace: {}", e);
        } catch (UnsupportedJwtException e) {
            log.info("Unsupported JWT token.");
            log.trace("Unsupported JWT token trace: {}", e);
        } catch (IllegalArgumentException e) {
            log.info("JWT token compact of handler are invalid.");
            log.trace("JWT token compact of handler are invalid trace: {}", e);
        }
        return false;
    }
}
