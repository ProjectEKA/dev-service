package in.projecteka.devservice.support;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.BatchGetValuesResponse;
import in.projecteka.devservice.support.model.ApprovedRequestsSheet;
import in.projecteka.devservice.support.model.SupportRequest;
import in.projecteka.devservice.support.model.SupportRequestProperties;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

import static in.projecteka.devservice.clients.ClientError.spreadsheetReadingFailed;

@AllArgsConstructor
public class SupportRequestService {
    private final Credential credential;
    private final SupportRequestRepository supportRequestRepository;
    private final SupportRequestProperties supportRequestProperties;
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final Logger logger = LoggerFactory.getLogger(SupportRequestService.class);

    public Mono<Void> processRequest(ApprovedRequestsSheet approvedRequestsSheet) {
        return readSpreadSheet(approvedRequestsSheet)
                .switchIfEmpty(Mono.defer(() -> Mono.error(spreadsheetReadingFailed())))
                .flatMap(readResult -> {
                    int rowNumber = 0;
                    for (var row : readResult.getValueRanges().get(0).getValues()) {
                        rowNumber++;
                        if (rowNumber > 1) {
                            SupportRequest supportRequest = getSupportRequest(row);
                            if (supportRequest == null) {
                                return Mono.error(spreadsheetReadingFailed());
                            }
                            supportRequestRepository.insert(supportRequest).subscribe();
                        }
                    }
                    return Mono.empty();
                });
    }

    private SupportRequest getSupportRequest(List<Object> row) {
        try {
            return SupportRequest.builder()
                    .name(row.get(1).toString())
                    .emailId(row.get(2).toString())
                    .phoneNumber(row.get(3).toString())
                    .organizationName(row.get(4).toString())
                    .expectedRoles(row.get(7).toString())
                    .status(row.get(10).toString())
                    .build();
        } catch (Exception exception) {
            logger.error("Error occurred --> " + exception);
            return null;
        }
    }

    private Mono<BatchGetValuesResponse> readSpreadSheet(ApprovedRequestsSheet approvedRequestsSheet) {
        try {
            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            Sheets service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                    .build();
            List<String> ranges = Arrays.asList(approvedRequestsSheet.getSheetName() + "!A1:Z");
            return Mono.just(service.spreadsheets().values()
                    .batchGet(supportRequestProperties.getSpreadsheetId())
                    .setRanges(ranges)
                    .execute());
        } catch (GoogleJsonResponseException exception) {
            logger.error("No sheet with name `" + approvedRequestsSheet.getSheetName() + "' found.");
            return Mono.empty();
        } catch (Exception exception) {
            logger.error("Error occurred --> " + exception);
            return Mono.empty();
        }
    }
}
