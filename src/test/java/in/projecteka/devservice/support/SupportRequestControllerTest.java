package in.projecteka.devservice.support;

import com.google.api.client.auth.oauth2.Credential;
import com.nimbusds.jose.jwk.JWKSet;
import in.projecteka.devservice.clients.ClientError;
import in.projecteka.devservice.common.Constants;
import in.projecteka.devservice.support.model.ApprovedRequestsSheet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static in.projecteka.devservice.common.Constants.PATH_SUPPORT_REQUEST;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;


@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class SupportRequestControllerTest {

    @Autowired
    private WebTestClient webClient;

    @MockBean
    private SupportRequestService supportRequestService;

    @Qualifier("supportRequestCredential")
    @MockBean
    private Credential credential;

    @MockBean
    JWKSet jwkSet;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void shouldReturnSuccessfulResponseFoValidApprovedRequestsSheet() throws GeneralSecurityException, IOException {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String currentDate = LocalDateTime.now().format(dateTimeFormatter);
        ApprovedRequestsSheet request = ApprovedRequestsSheet.builder().sheetName(currentDate).build();

        when(supportRequestService.processRequest(any(ApprovedRequestsSheet.class))).thenReturn(Mono.empty());

        webClient.post()
                .uri(PATH_SUPPORT_REQUEST)
                .contentType(APPLICATION_JSON)
                .body(BodyInserters.fromValue(request))
                .exchange().expectStatus().isOk();

    }

    @Test
    void shouldReturnErrorResponseForInValidApprovedRequestsSheet() throws GeneralSecurityException, IOException {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String currentDate = LocalDateTime.now().plusDays(Long.parseLong("5")).format(dateTimeFormatter);
        ApprovedRequestsSheet request = ApprovedRequestsSheet.builder().sheetName(currentDate).build();

        when(supportRequestService.processRequest(any(ApprovedRequestsSheet.class)))
                .thenReturn(Mono.error(ClientError.noSheetFound()));

        webClient.post()
                .uri(PATH_SUPPORT_REQUEST)
                .contentType(APPLICATION_JSON)
                .body(BodyInserters.fromValue(request))
                .exchange()
                .expectStatus()
                .isBadRequest();

    }
}