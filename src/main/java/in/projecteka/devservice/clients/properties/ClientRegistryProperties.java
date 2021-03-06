package in.projecteka.devservice.clients.properties;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConfigurationProperties(prefix = "devservice.clientregistry")
@AllArgsConstructor
@Getter
@ConstructorBinding
public class ClientRegistryProperties {
    private final String url;
}
