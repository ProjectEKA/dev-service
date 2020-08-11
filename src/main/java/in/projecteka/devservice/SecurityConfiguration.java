package in.projecteka.devservice;

import com.nimbusds.jose.jwk.JWKSet;
import in.projecteka.devservice.clients.properties.IdentityProperties;
import in.projecteka.devservice.common.Authenticator;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;

import static in.projecteka.devservice.clients.ClientError.unAuthorized;
import static in.projecteka.devservice.common.Constants.PATH_BRIDGES;
import static in.projecteka.devservice.common.Constants.PATH_HEARTBEAT;
import static org.springframework.util.StringUtils.hasText;
import static reactor.core.publisher.Mono.error;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfiguration {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(
            ServerHttpSecurity httpSecurity,
            ReactiveAuthenticationManager authenticationManager,
            ServerSecurityContextRepository securityContextRepository) {
        return httpSecurity
                .httpBasic().disable()
                .formLogin().disable()
                .csrf().disable()
                .logout().disable()
                .authorizeExchange()
                .pathMatchers(PATH_HEARTBEAT).permitAll().and()
                .authorizeExchange()
                .pathMatchers("/**")
                .authenticated().and()
                .authenticationManager(authenticationManager)
                .securityContextRepository(securityContextRepository)
                .build();
    }

    @Bean
    public ReactiveAuthenticationManager authenticationManager() {
        return new AuthenticationManager();
    }

    @Bean("JWKSet")
    public JWKSet jwkSet(IdentityProperties identityProperties) throws IOException, ParseException {
        return JWKSet.load(new URL(identityProperties.getJwkUrl()));
    }

    @Bean
    public Authenticator authenticator(@Qualifier("JWKSet") JWKSet jwkSet) {
        return new Authenticator(jwkSet);
    }

    @Bean
    public SecurityContextRepository contextRepository(Authenticator authenticator) {
        return new SecurityContextRepository(authenticator);
    }

    @AllArgsConstructor
    private static class SecurityContextRepository implements ServerSecurityContextRepository {
        private final Authenticator authenticator;

        @Override
        public Mono<Void> save(ServerWebExchange exchange, SecurityContext context) {
            throw new UnsupportedOperationException("No need right now!");
        }

        @Override
        public Mono<SecurityContext> load(ServerWebExchange exchange) {
            var token = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (!hasText(token)) {
                return Mono.empty();
            }
            return check(token).switchIfEmpty(error(unAuthorized()));
        }

        private Mono<SecurityContext> check(String token) {
            return authenticator.verify(token)
                    .map(caller -> new UsernamePasswordAuthenticationToken(
                            caller,
                            token,
                            new ArrayList<SimpleGrantedAuthority>()))
                    .map(SecurityContextImpl::new);
        }
    }

    private static class AuthenticationManager implements ReactiveAuthenticationManager {
        @Override
        public Mono<Authentication> authenticate(Authentication authentication) {
            var token = authentication.getCredentials().toString();
            var auth = new UsernamePasswordAuthenticationToken(
                    authentication.getPrincipal(),
                    token,
                    new ArrayList<SimpleGrantedAuthority>());
            return Mono.just(auth);
        }
    }
}
