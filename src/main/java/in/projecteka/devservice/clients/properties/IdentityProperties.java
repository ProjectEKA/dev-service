package in.projecteka.devservice.clients.properties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@Builder
@ConfigurationProperties(prefix = "devservice.keycloak")
@AllArgsConstructor
@ConstructorBinding
@Getter
public class IdentityProperties {
    private final String jwkUrl;
}
