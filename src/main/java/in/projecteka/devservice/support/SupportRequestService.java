package in.projecteka.devservice.support;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.BatchGetValuesResponse;
import in.projecteka.devservice.clients.ServiceAuthenticationClient;
import in.projecteka.devservice.clients.properties.GatewayServiceProperties;
import in.projecteka.devservice.support.model.ApprovedRequestsSheet;
import in.projecteka.devservice.support.model.CredentialRequest;
import in.projecteka.devservice.support.model.SupportBridgeRequest;
import in.projecteka.devservice.support.model.SupportBridgeResponse;
import in.projecteka.devservice.support.model.SupportRequest;
import in.projecteka.devservice.support.model.SupportRequestProperties;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static in.projecteka.devservice.clients.ClientError.networkServiceCallFailed;
import static in.projecteka.devservice.clients.ClientError.spreadsheetReadingFailed;

@AllArgsConstructor
public class SupportRequestService {
    private final Credential credential;
    private final SupportRequestRepository supportRequestRepository;
    private final SupportRequestProperties supportRequestProperties;
    private final ServiceAuthenticationClient serviceAuthenticationClient;
    private final GatewayServiceProperties gatewayServiceProperties;

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
                            String supportRequestId = UUID.randomUUID().toString();
                            SupportRequest supportRequest = getSupportRequest(row, supportRequestId);
                            if (supportRequest == null) {
                                return Mono.error(spreadsheetReadingFailed());
                            }
                            supportRequestRepository.insert(supportRequest).subscribe();
                        }
                    }
                    return Mono.empty();
                });
    }

    private SupportRequest getSupportRequest(List<Object> row, String supportRequestId) {
        try {
            return SupportRequest.builder()
                    .name(row.get(1).toString())
                    .emailId(row.get(2).toString())
                    .phoneNumber(row.get(3).toString())
                    .organizationName(row.get(4).toString())
                    .expectedRoles(row.get(7).toString())
                    .status(row.get(10).toString())
                    .supportRequestId(supportRequestId)
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

    public Mono<SupportBridgeResponse> generateIdAndSecret(CredentialRequest credentialRequest) {
        var supportRequest = supportRequestRepository.getSupportRequest(credentialRequest.getRequestId());
        var session = serviceAuthenticationClient.getTokenFor(gatewayServiceProperties.getUsername(),
                gatewayServiceProperties.getPassword());

        return Mono.zip(supportRequest, session)
                .flatMap(tuple -> {
                    try {
                        String hash = getSHA(tuple.getT1().getExpectedRoles() + tuple.getT1().getSupportRequestId());
                        var clientId = hash.substring(0, 7);
                        var supportBridgeRequest = createSupportBridgeRequest(tuple.getT1(), clientId);
                        return serviceAuthenticationClient.getClientIdAndSecret(supportBridgeRequest, tuple.getT2().getAccessToken());
                    } catch (NoSuchAlgorithmException e) {
                        logger.error("Error while creating hash");
                        return Mono.error(networkServiceCallFailed());
                    }
                });
    }

    private SupportBridgeRequest createSupportBridgeRequest(SupportRequest supportRequest, String clientId) {
        return SupportBridgeRequest.builder()
                .id(clientId)
                .name(supportRequest.getOrganizationName())
                .url("")
                .active(true)
                .blocklisted(false)
                .build();
    }

    private static String getSHA(String input) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        var hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
        BigInteger number = new BigInteger(1, hash);
        StringBuilder hexString = new StringBuilder(number.toString(16));
        while (hexString.length() < 32) {
            hexString.insert(0, '0');
        }
        return hexString.toString();
    }
}
