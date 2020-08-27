package in.projecteka.devservice.email;


import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConfigurationProperties(prefix = "devservice.googleservice")
@AllArgsConstructor
@Getter
@ConstructorBinding
public class GoogleServiceProperties {
    private String credentialPath;
    private String sheetId;
    private Boolean enabled;
}
