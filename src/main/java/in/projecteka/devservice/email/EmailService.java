package in.projecteka.devservice.email;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;
import in.projecteka.devservice.clients.ClientError;
import in.projecteka.devservice.email.model.EmailRequest;
import lombok.AllArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

@AllArgsConstructor
public class EmailService {
    private static final String APPLICATION_NAME = "My APP";
    private JavaMailSender javaMailSender;
    private EmailProperties emailProperties;

    public Mono<Void> sendEmail(EmailRequest emailRequest) {
        return Mono.create(monoSink -> {
            try {
//                SimpleMailMessage msg = new SimpleMailMessage();
//                msg.setTo(emailProperties.getReceiver());
//                msg.setFrom(emailProperties.getSender());
//                msg.setSubject(emailProperties.getSubject());
//                msg.setText(String.format("Please find the details \n\n" +
//                                "Name: %s \n" +
//                                "Email address: %s \n" +
//                                "Organization you represent: %s \n" +
//                                "Organizations Ids you serve: %s\n" +
//                                "Intent: %s\n" +
//                                "Endpoint: %s \n\n" +
//                                "Regards\n" +
//                                "%s",
//                        emailRequest.getName(),
//                        emailRequest.getEmail(),
//                        emailRequest.getRepOrg(),
//                        emailRequest.getServeOrgId(),
//                        emailRequest.getIntent(),
//                        emailRequest.getEndPoint(),
//                        emailProperties.getSender()));
//
//                javaMailSender.send(msg);

                final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
                final String spreadsheetId = "1CWgChfwZCTtQAdMGi1k7T5-dM8K2pKtp-odJ4p7RxlM";
                final String range = "Data!A1:B1";
                Sheets service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials())
                        .setApplicationName(APPLICATION_NAME)
                        .build();
                ValueRange newRecord = new ValueRange()
                        .setRange(range)
                        .setValues(List.of(List.of(emailRequest.getRepOrg(), emailRequest.getName())));

                service.spreadsheets().values()
                        .append(spreadsheetId, range, newRecord)
                        .setValueInputOption("RAW")
                        .execute();

                monoSink.success();
            } catch (Exception e) {
                monoSink.error(ClientError.networkServiceCallFailed());
            }
        });
    }

    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS);
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

    /**
     * Creates an authorized Credential object.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private static Credential getCredentials() throws IOException {
        // Load client secrets.
        InputStream in = EmailService.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        return GoogleCredential.fromStream(in)
                .createScoped(SCOPES);
    }
}
