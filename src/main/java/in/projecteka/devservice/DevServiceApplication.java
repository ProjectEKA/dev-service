package in.projecteka.devservice;

import in.projecteka.devservice.clients.properties.ClientRegistryProperties;
import in.projecteka.devservice.clients.properties.GatewayServiceProperties;
import in.projecteka.devservice.clients.properties.IdentityProperties;
import in.projecteka.devservice.email.EmailProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({
        IdentityProperties.class,
        GatewayServiceProperties.class,
        ClientRegistryProperties.class,
        EmailProperties.class
})
public class DevServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(DevServiceApplication.class, args);
    }
}
