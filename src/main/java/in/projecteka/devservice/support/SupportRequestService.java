package in.projecteka.devservice.support;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.BatchGetValuesResponse;
import in.projecteka.devservice.clients.ClientError;
import in.projecteka.devservice.support.model.ApprovedRequest;
import in.projecteka.devservice.support.model.SupportRequest;
import in.projecteka.devservice.support.model.SupportRequestProperties;
import lombok.AllArgsConstructor;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;

@AllArgsConstructor
public class SupportRequestService {
    private final Credential credential;
    private final SupportRequestRepository supportRequestRepository;
    private final SupportRequestProperties supportRequestProperties;
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();


    public Mono<Void> processRequest(ApprovedRequest approvedRequest) throws GeneralSecurityException, IOException {
        return readSpreadSheet(approvedRequest)
                .switchIfEmpty(Mono.error(ClientError.noSheetFound()))
                .flatMap(readResult -> {
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
                });


    }

    private Mono<BatchGetValuesResponse> readSpreadSheet(ApprovedRequest approvedRequest) {
        try {
            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            Sheets service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                    .build();
            List<String> ranges = Arrays.asList(approvedRequest.getSheetName() + "!A1:Z");
            return Mono.just(service.spreadsheets().values()
                    .batchGet(supportRequestProperties.getSpreadsheetId())
                    .setRanges(ranges)
                    .execute());
        } catch (Exception e) {
            return Mono.empty();
        }
    }
}
