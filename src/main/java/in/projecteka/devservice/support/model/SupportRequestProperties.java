package in.projecteka.devservice.support.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConfigurationProperties(prefix = "devservice.support-requests")
@AllArgsConstructor
@Getter
@ConstructorBinding
public class SupportRequestProperties {
    private final String spreadsheetId;
    private final String credentialPath;
}
