package in.projecteka.devservice.clients;

import in.projecteka.devservice.bridge.model.ServiceType;
import in.projecteka.devservice.clients.model.Session;
import lombok.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Properties;

import static in.projecteka.devservice.clients.ClientError.networkServiceCallFailed;
import static in.projecteka.devservice.clients.ClientError.unAuthorized;
import static in.projecteka.devservice.clients.ClientError.unprocessableEntity;
import static java.lang.String.format;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static reactor.core.publisher.Mono.error;

public class ServiceAuthenticationClient {
    private final Logger logger = LoggerFactory.getLogger(ServiceAuthenticationClient.class);
    private final WebClient webClient;

    public ServiceAuthenticationClient(WebClient.Builder webClient, String baseUrl) {
        this.webClient = webClient.baseUrl(baseUrl).build();
    }

    private SessionRequest requestWith(String username, String password) {
        return new SessionRequest(username, password);
    }

    public Mono<Session> getTokenFor(String username, String password) {
        return webClient
                .post()
                .uri("/user/sessions")
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .body(BodyInserters.fromValue(requestWith(username, password)))
                .retrieve()
                .onStatus(HttpStatus::isError, clientResponse -> clientResponse.bodyToMono(Properties.class)
                        .doOnNext(properties -> logger.error(properties.toString()))
                        .thenReturn(unAuthorized()))
                .bodyToMono(Session.class);
    }

    private BridgeUpdateRequest bridgeRequestWith(String id, String url) {
        return new BridgeUpdateRequest(id, url);
    }

    public Mono<Void> updateBridgeWith(String bridgeId, String url, String token) {
        return webClient.put()
                .uri("/internal/bridges")
                .contentType(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION, token)
                .body(Mono.just(bridgeRequestWith(bridgeId, url)), BridgeUpdateRequest.class)
                .retrieve()
                .onStatus(httpStatus -> httpStatus.value() == HttpStatus.BAD_REQUEST.value(),
                        clientResponse -> clientResponse.bodyToMono(Properties.class)
                                .doOnNext(properties -> logger.error(properties.toString()))
                                .then(error(unprocessableEntity())))
                .onStatus(httpStatus -> httpStatus.value() == HttpStatus.UNAUTHORIZED.value(),
                        clientResponse -> clientResponse.bodyToMono(Properties.class)
                                .doOnNext(properties -> logger.error(properties.toString()))
                                .then(error(unAuthorized())))
                .onStatus(HttpStatus::is5xxServerError,
                        clientResponse -> clientResponse.bodyToMono(Properties.class)
                                .doOnNext(properties -> logger.error(properties.toString()))
                                .then(error(networkServiceCallFailed())))
                .toBodilessEntity()
                .then();
    }

    private BridgeServiceRequest bridgeServiceRequest(String id, String name, ServiceType type, boolean active) {
        return new BridgeServiceRequest(id, name, type, active);
    }

    public Mono<Void> upsertBridgeServiceEntry(String bridgeId, String id,
                                               String name, ServiceType type,
                                               boolean active, Session session
    ) {
        return webClient.put()
                .uri(format("/internal/bridges/%s/services", bridgeId))
                .contentType(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION, format("%s %s", session.getTokenType(), session.getAccessToken()))
                .body(Mono.just(List.of(bridgeServiceRequest(id, name, type, active))), BridgeServiceRequest.class)
                .retrieve()
                .onStatus(httpStatus -> httpStatus.value() == HttpStatus.BAD_REQUEST.value(),
                        clientResponse -> clientResponse.bodyToMono(Properties.class)
                                .doOnNext(properties -> logger.error(properties.toString()))
                                .then(error(unprocessableEntity())))
                .onStatus(httpStatus -> httpStatus.value() == HttpStatus.UNAUTHORIZED.value(),
                        clientResponse -> clientResponse.bodyToMono(Properties.class)
                                .doOnNext(properties -> logger.error(properties.toString()))
                                .then(error(unAuthorized())))
                .onStatus(HttpStatus::is5xxServerError,
                        clientResponse -> clientResponse.bodyToMono(Properties.class)
                                .doOnNext(properties -> logger.error(properties.toString()))
                                .then(error(networkServiceCallFailed())
                                ))
                .toBodilessEntity()
                .then();
    }


    @Value
    private static class BridgeServiceRequest {
        String id;
        String name;
        ServiceType type;
        boolean active;
    }

    @Value
    private static class BridgeUpdateRequest {
        String id;
        String url;
    }

    @Value
    private static class SessionRequest {
        String username;
        String password;
    }
}
