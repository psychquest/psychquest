package io.psychquest.app.infra.config;

import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.CorsFilter;
import org.zalando.problem.spring.web.advice.security.SecurityProblemSupport;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import io.psychquest.app.infra.domain.security.jwt.JwtConfigurer;
import io.psychquest.app.infra.domain.security.jwt.TokenProvider;
import io.psychquest.app.infra.domain.security.twofa.TwoFAAuthenticationProvider;

import static io.psychquest.app.infra.domain.security.AuthoritiesConstants.ADMIN;
import static io.psychquest.app.infra.domain.security.AuthoritiesConstants.PRE_AUTH_USER;
import static io.psychquest.app.infra.domain.security.AuthoritiesConstants.USER;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
@Import(SecurityProblemSupport.class)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    private static final String ALGORITHM_ID_FOR_ENCODE = "pbkdf2";
    private static final int DEFAULT_HASH_WIDTH = 256;
    private static final int DEFAULT_ITERATIONS = 185000;

    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final UserDetailsService userDetailsService;
    private final CorsFilter corsFilter;
    private final SecurityProblemSupport problemSupport;
    private final TokenProvider tokenProvider;
    private final String secret;
    private final TwoFAAuthenticationProvider twoFAAuthenticationProvider;


    public SecurityConfiguration(ApplicationProperties applicationProperties,
                                 AuthenticationManagerBuilder authenticationManagerBuilder,
                                 UserDetailsService userDetailsService,
                                 CorsFilter corsFilter,
                                 SecurityProblemSupport problemSupport,
                                 TokenProvider tokenProvider, TwoFAAuthenticationProvider twoFAAuthenticationProvider) {
        this.authenticationManagerBuilder = authenticationManagerBuilder;
        this.userDetailsService = userDetailsService;
        this.corsFilter = corsFilter;
        this.problemSupport = problemSupport;
        this.tokenProvider = tokenProvider;
        secret = applicationProperties.getPasswordHashingSecret();
        this.twoFAAuthenticationProvider = twoFAAuthenticationProvider;
    }

    @PostConstruct
    public void init() {
        try {
            authenticationManagerBuilder
                .userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder())
                .and()
                .authenticationProvider(twoFAAuthenticationProvider);
        } catch (Exception e) {
            throw new BeanInitializationException("Security configuration failed", e);
        }
    }

    @Override
    @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        Map<String, PasswordEncoder> encoders = new HashMap<>();
        Pbkdf2PasswordEncoder encoder = new Pbkdf2PasswordEncoder(secret, DEFAULT_ITERATIONS, DEFAULT_HASH_WIDTH);
        encoders.put(ALGORITHM_ID_FOR_ENCODE, encoder);
        return new DelegatingPasswordEncoder(ALGORITHM_ID_FOR_ENCODE, encoders);
    }

    @Override
    public void configure(WebSecurity web) {
        web.ignoring()
            .antMatchers(HttpMethod.OPTIONS, "/**")
            .antMatchers("/app/**/*.{js,html}");
    }


    @Override
    public void configure(HttpSecurity http) throws Exception {
        // @formatter:off
        http
            .addFilterBefore(corsFilter, UsernamePasswordAuthenticationFilter.class)
            .exceptionHandling()
            .authenticationEntryPoint(problemSupport)
            .accessDeniedHandler(problemSupport)
         .and()
            .csrf()
            .disable()
            .headers()
            .frameOptions()
            .disable()
         .and()
            .sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
         .and()
            .authorizeRequests()
            .antMatchers("/api/register").permitAll()
            .antMatchers("/api/activate").permitAll()
            .antMatchers("/api/authenticate").permitAll()
            .antMatchers("/api/authenticate/2fa").hasAuthority(PRE_AUTH_USER)
            .antMatchers("/api/account/reset-password/init").permitAll()
            .antMatchers("/api/account/reset-password/finish").permitAll()
            .antMatchers("/api/**").hasAnyAuthority(USER, ADMIN)
        .and()
            .apply(securityConfigurerAdapter());
        // @formatter:on
    }

    private JwtConfigurer securityConfigurerAdapter() {
        return new JwtConfigurer(tokenProvider);
    }

}
