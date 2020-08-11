package in.projecteka.devservice;

import in.projecteka.devservice.clients.properties.GatewayServiceProperties;
import in.projecteka.devservice.clients.properties.IdentityProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({IdentityProperties.class,
                                GatewayServiceProperties.class})
public class DevServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(DevServiceApplication.class, args);
    }
}
