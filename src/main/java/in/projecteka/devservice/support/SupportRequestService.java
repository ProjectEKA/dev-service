package in.projecteka.devservice.support;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.BatchGetValuesResponse;
import in.projecteka.devservice.support.model.SupportRequest;
import in.projecteka.devservice.support.model.SupportRequestProperties;
import lombok.AllArgsConstructor;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

@AllArgsConstructor
public class SupportRequestService {
    private final Credential credential;
    private final SupportRequestRepository supportRequestRepository;
    private final SupportRequestProperties supportRequestProperties;
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();


    public Mono<Void> processRequest() throws GeneralSecurityException, IOException {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Sheets service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                .build();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String currentDate = LocalDateTime.now().format(dateTimeFormatter);
        List<String> ranges = Arrays.asList(currentDate + "!A1:Z");
        BatchGetValuesResponse readResult = service.spreadsheets().values()
                .batchGet(supportRequestProperties.getSpreadsheetId())
                .setRanges(ranges)
                .execute();

        for (var row : readResult.getValueRanges().get(0).getValues()) {
            var supportRequest = SupportRequest.builder()
                    .name(row.get(1).toString())
                    .emailId(row.get(2).toString())
                    .phoneNumber(row.get(3).toString())
                    .organizationName(row.get(4).toString())
                    .expectedRoles(row.get(7).toString())
                    .status(row.get(10).toString())
                    .build();
            supportRequestRepository.insert(supportRequest).subscribe();
        }
        return Mono.empty();
    }
}
