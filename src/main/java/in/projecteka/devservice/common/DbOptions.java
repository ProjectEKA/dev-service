package in.projecteka.devservice.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConstructorBinding
@ConfigurationProperties("devservice.db")
@Getter
@AllArgsConstructor
public class DbOptions {
    private final String host;
    private final int port;
    private final String schema;
    private final String user;
    private final String password;
    private final int poolSize;
}
