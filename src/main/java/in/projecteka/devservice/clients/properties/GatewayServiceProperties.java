package in.projecteka.devservice.clients.properties;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConfigurationProperties(prefix = "devservice.gatewayservice")
@Getter
@AllArgsConstructor
@ConstructorBinding
public class GatewayServiceProperties {
    private final String baseUrl;
    private final String username;
    private final String password;
}
