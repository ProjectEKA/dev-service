package in.projecteka.devservice.support;

import com.google.api.client.auth.oauth2.Credential;
import in.projecteka.devservice.bridge.TestBuilders;
import in.projecteka.devservice.clients.ServiceAuthenticationClient;
import in.projecteka.devservice.clients.properties.GatewayServiceProperties;
import in.projecteka.devservice.support.model.SupportBridgeRequest;
import in.projecteka.devservice.support.model.SupportRequestProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SupportRequestServiceTest {

    @Mock
    GatewayServiceProperties gatewayServiceProperties;

    @Mock
    SupportRequestRepository supportRequestRepository;

    @Mock
    ServiceAuthenticationClient serviceAuthenticationClient;

    @Mock
    Credential credential;

    @Mock
    SupportRequestProperties supportRequestProperties;

    SupportRequestService supportRequestService;

    @BeforeEach
    void init() {
        MockitoAnnotations.initMocks(this);
        supportRequestService = Mockito.spy(new SupportRequestService(
                credential,
                supportRequestRepository,
                supportRequestProperties,
                serviceAuthenticationClient,
                gatewayServiceProperties
        ));
    }

    @Test
    void shouldGenerateClientIdAndSecret() {
        var supportRequest = TestBuilders.supportRequest().build();
        var supportBridgeResponse = TestBuilders.supportBridgeResponse().build();
        var session = TestBuilders.session().build();
        var username = TestBuilders.string();
        var password = TestBuilders.string();
        var credentialRequest = TestBuilders.credentialRequest().build();

        when(supportRequestRepository.getSupportRequest(credentialRequest.getRequestId())).thenReturn(Mono.just(supportRequest));
        when(serviceAuthenticationClient.getTokenFor(username, password)).thenReturn(Mono.just(session));
        when(serviceAuthenticationClient.getClientIdAndSecret(any(SupportBridgeRequest.class), anyString()))
                .thenReturn(Mono.just(supportBridgeResponse));
        when(gatewayServiceProperties.getUsername()).thenReturn(username);
        when(gatewayServiceProperties.getPassword()).thenReturn(password);

        StepVerifier.create(supportRequestService.generateIdAndSecret(credentialRequest))
                .assertNext(response -> assertThat(response).isEqualTo(supportBridgeResponse))
                .verifyComplete();

        verify(serviceAuthenticationClient).getTokenFor(username, password);
        verify(serviceAuthenticationClient).getClientIdAndSecret(any(SupportBridgeRequest.class), anyString());
        verify(supportRequestRepository).getSupportRequest(anyString());
    }
}