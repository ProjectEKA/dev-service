package in.projecteka.devservice.bridge;

import com.google.api.client.auth.oauth2.Credential;
import com.nimbusds.jose.jwk.JWKSet;
import in.projecteka.devservice.bridge.model.BridgeServiceRequest;
import in.projecteka.devservice.bridge.model.OrganizationDetails;
import in.projecteka.devservice.clients.ClientRegistryClient;
import in.projecteka.devservice.clients.ServiceAuthenticationClient;
import in.projecteka.devservice.clients.properties.GatewayServiceProperties;
import in.projecteka.devservice.common.Authenticator;
import in.projecteka.devservice.common.Caller;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;

import static in.projecteka.devservice.bridge.TestBuilders.bridgeRequest;
import static in.projecteka.devservice.bridge.TestBuilders.session;
import static in.projecteka.devservice.bridge.TestBuilders.string;
import static in.projecteka.devservice.bridge.model.ServiceType.HIP;
import static in.projecteka.devservice.common.Constants.PATH_BRIDGES;
import static in.projecteka.devservice.common.Constants.PATH_BRIDGE_SERVICES;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static reactor.core.publisher.Mono.just;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
public class BridgeUserJourneyTest {
    @Autowired
    WebTestClient webTestClient;

    @MockBean
    JavaMailSender javaMailSender;

    @MockBean
    JWKSet jwkSet;
    @MockBean
    ServiceAuthenticationClient serviceAuthenticationClient;
    @MockBean
    ClientRegistryClient clientRegistryClient;
    @MockBean
    GatewayServiceProperties gatewayServiceProperties;
    @MockBean
    private Authenticator authenticator;

    @Qualifier("supportRequestCredential")
    @MockBean
    private Credential credential;

    @Test
    void shouldUpdateBridgeUrl() {
        var token = string();
        var bridgeId = string();
        var bridgeRequest = bridgeRequest().build();
        var username = string();
        var password = string();
        var caller = Caller.builder().clientId(bridgeId).build();
        var session = session().build();
        when(authenticator.verify(token)).thenReturn(just(caller));
        when(gatewayServiceProperties.getUsername()).thenReturn(username);
        when(gatewayServiceProperties.getPassword()).thenReturn(password);
        when(serviceAuthenticationClient.getTokenFor(username, password)).thenReturn(just(session));
        when(serviceAuthenticationClient.updateBridgeWith(bridgeId,
                bridgeRequest.getUrl(),
                session.getTokenType() + " " + session.getAccessToken())).thenReturn(Mono.empty());

        webTestClient
                .patch()
                .uri(PATH_BRIDGES)
                .header(AUTHORIZATION, token)
                .contentType(APPLICATION_JSON)
                .bodyValue(bridgeRequest)
                .exchange()
                .expectStatus()
                .isOk();
    }

    @Test
    void shouldUpsertBridgeServiceRequest() {
        var token = string();
        var bridgeId = string();
        var username = string();
        var password = string();
        var session = session().build();
        var caller = Caller.builder().clientId(bridgeId).build();
        var alias = new ArrayList<String>();
        var request = BridgeServiceRequest.builder()
                .id("service_id")
                .type(HIP)
                .city("random")
                .alias(alias)
                .name("hospital_name").build();
        var orgDetails = OrganizationDetails.builder()
                .id(request.getId())
                .name(request.getName())
                .city(request.getCity())
                .orgAlias(request.getAlias())
                .serviceType(request.getType()).build();

        when(authenticator.verify(token)).thenReturn(just(caller));
        when(gatewayServiceProperties.getUsername()).thenReturn(username);
        when(gatewayServiceProperties.getPassword()).thenReturn(password);
        when(serviceAuthenticationClient.getTokenFor(username, password)).thenReturn(just(session));
        when(serviceAuthenticationClient.upsertBridgeServiceEntry(
                bridgeId, request.getId(), request.getName(), request.getType(), request.isActive(), session
        )).thenReturn(Mono.empty());
        when(clientRegistryClient.addOrganization(orgDetails)).thenReturn(Mono.empty());

        webTestClient
                .put()
                .uri(PATH_BRIDGE_SERVICES)
                .header(AUTHORIZATION, token)
                .contentType(APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus()
                .isOk();
    }
}
