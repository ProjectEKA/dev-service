package in.projecteka.devservice.email;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import in.projecteka.devservice.clients.ClientError;
import in.projecteka.devservice.email.model.Field;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import reactor.core.publisher.Mono;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
public class EmailService {
    private static Logger logger = LoggerFactory.getLogger(EmailService.class);
    private JavaMailSender javaMailSender;
    private EmailProperties emailProperties;
    private GoogleServiceProperties googleServiceProperties;
    private Credential credential;
    private String autoResponseEmailBody;

    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    public Mono<Void> processRequest(List<Field> emailFormFields) {
        return Mono.create(monoSink -> {
            try {
                String body = String.format("Please find the details \n\n" +
                        "%s \n\n" +
                        "Regards\n" +
                        "%s", generateMessage(emailFormFields), emailProperties.getSender());
                sendEmail(emailProperties.getReceiver(), emailProperties.getSubject(), body, false);

                if (emailProperties.isAutoResponseEnabled()) {
                    sendAutoResponse(extractEmail(emailFormFields));
                }
                if (googleServiceProperties.getEnabled()) {
                    insertEntryInSheet(emailFormFields);
                }
                monoSink.success();
            } catch (Exception e) {
                logger.error("Error Occurred -->", e);
                monoSink.error(ClientError.emailSendingFailed());
            }
        });
    }

    private String extractEmail(List<Field> emailFormFields) {
        return emailFormFields.stream().filter(field -> field.getTag().equals("email")).findFirst().get().getValue();
    }

    private void sendAutoResponse(String to) throws MessagingException {
        sendEmail(to, emailProperties.getAutoResponseSubject(), autoResponseEmailBody, true);
    }

    private void insertEntryInSheet(List<Field> emailFormFields) throws GeneralSecurityException, IOException {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        final String spreadsheetId = googleServiceProperties.getSheetId();
        final String range = "Data!A1:B1";
        List<Object> newRow = emailFormFields.stream().map(Field::getValue).collect(Collectors.toList());
        newRow.add(0, getFormattedDate());
        Sheets service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                .build();
        ValueRange newRecord = new ValueRange()
                .setRange(range)
                .setValues(List.of(newRow));

        service.spreadsheets().values()
                .append(spreadsheetId, range, newRecord)
                .setValueInputOption("USER_ENTERED")
                .execute();
    }

    private void sendEmail(String to, String subject, String body, boolean isHTML) throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper messageHelper = new MimeMessageHelper(message);
        messageHelper.setTo(to);
        messageHelper.setFrom(emailProperties.getSender());
        messageHelper.setSubject(subject);
        messageHelper.setText(body, isHTML);
        javaMailSender.send(message);
    }

    private String generateMessage(List<Field> fields) {
        return fields.stream()
                .map(f -> String.format("%s: %s", f.getTitle(),
                        f.getValue()))
                .collect(Collectors.joining("\n"));
    }

    private String getFormattedDate() {
        ZonedDateTime istTime = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));
        DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("MM/dd/yyyy - HH:mm:ss ");
        return istTime.format(formatter2);
    }

}
