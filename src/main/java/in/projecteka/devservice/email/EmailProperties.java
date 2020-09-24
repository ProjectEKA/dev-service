package in.projecteka.devservice.email;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConfigurationProperties(prefix = "devservice.email")
@AllArgsConstructor
@Getter
@ConstructorBinding
public class EmailProperties {
    private String clientName;
    private String sender;
    private String receiver;
    private String subject;
    private String autoResponseBodyPath;
    private String autoResponseSubject;
    private boolean autoResponseEnabled;
}
