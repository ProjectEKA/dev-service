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
import in.projecteka.devservice.email.model.Field;
import lombok.AllArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import reactor.core.publisher.Mono;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
public class EmailService {
    private static final String APPLICATION_NAME = "My APP";
    private JavaMailSender javaMailSender;
    private EmailProperties emailProperties;
    private GoogleServiceProperties googleServiceProperties;

    public Mono<Void> sendEmail(List<Field> emailFormFields) {
        return Mono.create(monoSink -> {
            try {
                SimpleMailMessage msg = new SimpleMailMessage();
                msg.setTo(emailProperties.getReceiver());
                msg.setFrom(emailProperties.getSender());
                msg.setSubject(emailProperties.getSubject());

                msg.setText(String.format("Please find the details \n\n" +
                                "%s \n\n" +
                                "Regards\n" +
                                "%s", generateMessage(emailFormFields),emailProperties.getSender() ));

                javaMailSender.send(msg);

                final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
                final String spreadsheetId = googleServiceProperties.getSheetId();
                final String range = "Data!A1:B1";
                List<Object> newRow = emailFormFields.stream().map(Field::getValue).collect(Collectors.toList());
                newRow.add(0, getFormattedDate());
                Sheets service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY,
                        getCredentials(googleServiceProperties.getCredentialPath()))
                        .setApplicationName(APPLICATION_NAME)
                        .build();
                ValueRange newRecord = new ValueRange()
                        .setRange(range)
                        .setValues(List.of(newRow));

                service.spreadsheets().values()
                        .append(spreadsheetId, range, newRecord)
                        .setValueInputOption("USER_ENTERED")
                        .execute();

                monoSink.success();
            } catch (Exception e) {
                monoSink.error(ClientError.emailSendingFailed());
            }
        });
    }

    private String generateMessage(List<Field> fields){
        return fields.stream()
                .map(f -> String.format("%s: %s", f.getTitle(),
                        f.getValue()))
                .collect(Collectors.joining("\n"));
    }

    private String getFormattedDate(){
        ZonedDateTime istTime = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));
        DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("MM/dd/yyyy - HH:mm:ss ");
        return istTime.format(formatter2);
    }

    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS);

    private static Credential getCredentials(String path) throws IOException {
        return GoogleCredential.fromStream(new FileInputStream(path))
                .createScoped(SCOPES);
    }
}
