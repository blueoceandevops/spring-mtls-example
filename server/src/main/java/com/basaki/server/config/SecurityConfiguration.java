package com.basaki.server.config;

import com.basaki.server.error.exception.SecurityConfigurationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;

/**
 * {@code SecurityConfiguration} is the base Spring security
 * configuration for Eureka server. It can handle multiple users.
 * <p/>
 *
 * @author Indra Basak
 * @since 02/24/18
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@EnableConfigurationProperties(SecurityAuthProperties.class)
@Slf4j
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    private final SecurityAuthProperties properties;

    @Autowired
    public SecurityConfiguration(SecurityAuthProperties properties) {
        this.properties = properties;
    }

    /**
     * Configures HTTP security access control by restricting endpoints based on
     * roles. The restricted service id are declared as security.auth.*
     * properties.
     * <pre>
     * security:
     *   auth:
     *     endpoints:
     *       endpoint1:
     *         path: /books
     *         methods: POST
     *         roles: BOOK_WRITE
     *       endpoint2:
     *         path: /books/**
     *         methods: GET
     *         roles: BOOK_WRITE, BOOK_READ
     * </pre>
     *
     * @param http the HTTP security object to be configured.
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        properties.getEndpoints().forEach((key, value) -> {
            try {
                for (HttpMethod method : value.getMethods()) {
                    http.authorizeRequests()
                            .antMatchers(method, value.getPath())
                            .hasAnyAuthority(value.getRoles())
                            .and()
                            .x509()
                            .subjectPrincipalRegex("CN=(.*?)(?:,|$)")
                            .userDetailsService(userDetailsService())
                            .and().csrf().disable();

                    log.info("Adding security for path " + value.getPath()
                            + " and method " + method);
                }
            } catch (Exception e) {
                throw new SecurityConfigurationException(
                        "Problem encountered while setting up " +
                                "endpoint restrictions", e);
            }
        });

        http.sessionManagement().sessionCreationPolicy(
                SessionCreationPolicy.STATELESS);
    }

    @Override
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            log.info("Trying to load user: " + username);

            SecurityAuthProperties.User
                    user = properties.getUsers().get(username);
            if (user != null) {
                log.info("Successful in adding user " + username);

                return new User(username, "",
                        AuthorityUtils.createAuthorityList(
                                user.getRoles()));
            }

            log.info("Failed to add user " + username);
            return null;
        };
    }

    // NOT WORKING
    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/v2/api-docs/**");
        web.ignoring().antMatchers("/swagger.json");
        web.ignoring().antMatchers("/swagger-ui.html");
        web.ignoring().antMatchers("/swagger-resources/**");
        web.ignoring().antMatchers("/webjars/**");
    }
}
