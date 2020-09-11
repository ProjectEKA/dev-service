package in.projecteka.devservice.bridge;

import com.google.api.client.auth.oauth2.Credential;
import in.projecteka.devservice.bridge.model.BridgeServiceRequest;
import in.projecteka.devservice.bridge.model.OrganizationDetails;
import in.projecteka.devservice.bridge.model.ServiceType;
import in.projecteka.devservice.clients.ClientRegistryClient;
import in.projecteka.devservice.clients.ServiceAuthenticationClient;
import in.projecteka.devservice.clients.properties.GatewayServiceProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.mock.mockito.MockBean;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static in.projecteka.devservice.bridge.TestBuilders.bridgeRequest;
import static in.projecteka.devservice.bridge.TestBuilders.session;
import static in.projecteka.devservice.bridge.TestBuilders.string;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static reactor.core.publisher.Mono.just;

public class BridgeTest {
    @Mock
    ServiceAuthenticationClient serviceAuthenticationClient;

    @Mock
    GatewayServiceProperties gatewayServiceProperties;

    @Mock
    ClientRegistryClient clientRegistryClient;

    private BridgeService bridgeService;

    @BeforeEach
    void init() {
        MockitoAnnotations.initMocks(this);
        bridgeService = Mockito.spy(new BridgeService(
                serviceAuthenticationClient,
                clientRegistryClient,
                gatewayServiceProperties
        ));
    }

    @Test
    void shouldUpdateBridgeUrl() {
        var bridgeId = string();
        var bridgeRequest = bridgeRequest().build();
        var username = string();
        var password = string();
        var session = session().build();
        when(gatewayServiceProperties.getUsername()).thenReturn(username);
        when(gatewayServiceProperties.getPassword()).thenReturn(password);
        when(serviceAuthenticationClient.getTokenFor(username, password)).thenReturn(just(session));
        when(serviceAuthenticationClient.updateBridgeWith(bridgeId,
                bridgeRequest.getUrl(),
                session.getTokenType() + " " + session.getAccessToken())).thenReturn(Mono.empty());

        var bridgeProducer = bridgeService.updateBridgeUrl(bridgeId, bridgeRequest);
        StepVerifier.create(bridgeProducer)
                .verifyComplete();

        verify(serviceAuthenticationClient).getTokenFor(username, password);
        verify(serviceAuthenticationClient).updateBridgeWith(bridgeId,
                bridgeRequest.getUrl(),
                session.getTokenType() + " " + session.getAccessToken());
    }

    @Test
    void shouldPopulateBridgeServiceEntry() {
        var bridgeId = string();
        var username = string();
        var password = string();
        var session = session().build();
        var request = BridgeServiceRequest.builder()
                .active(true)
                .city(string())
                .id(string())
                .name(string())
                .type(ServiceType.HIP)
                .build();
        var orgDetails = OrganizationDetails.builder()
                .id(request.getId())
                .name(request.getName())
                .city(request.getCity())
                .orgAlias(request.getAlias())
                .serviceType(request.getType()).build();

        when(gatewayServiceProperties.getUsername()).thenReturn(username);
        when(gatewayServiceProperties.getPassword()).thenReturn(password);
        when(serviceAuthenticationClient.getTokenFor(username, password)).thenReturn(just(session));
        when(serviceAuthenticationClient.upsertBridgeServiceEntry(
                bridgeId, request.getId(), request.getName(), request.getType(), request.isActive(), session
        )).thenReturn(Mono.empty());
        when(clientRegistryClient.addOrganization(orgDetails)).thenReturn(Mono.empty());

        StepVerifier.create(bridgeService.upsertBridgeServiceEntry(bridgeId, request)).verifyComplete();

        verify(serviceAuthenticationClient).getTokenFor(username, password);
        verify(serviceAuthenticationClient).upsertBridgeServiceEntry(
                bridgeId, request.getId(), request.getName(), request.getType(), request.isActive(), session
        );
        verify(clientRegistryClient).addOrganization(orgDetails);

    }
}
