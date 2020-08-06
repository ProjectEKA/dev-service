package in.projecteka.devservice.clients;

import in.projecteka.devservice.clients.model.Session;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.util.Properties;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;

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
                        .thenReturn(ClientError.unAuthorized()))
                .bodyToMono(Session.class);
    }

    private BridgeRequest bridgeRequestWith(String id, String url) {
        return new BridgeRequest(id, url);
    }

    public Mono<Void> updateBridgeWith(String bridgeId, String url, String token, String gatewayBaseUrl) {
        return webClient.post()
                .uri(gatewayBaseUrl + "/internal/bridges")
                .contentType(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION, token)
                .bodyValue(BodyInserters.fromValue(bridgeRequestWith(bridgeId, url)))
                .retrieve()
                .onStatus(httpStatus -> httpStatus.value() == HttpStatus.BAD_REQUEST.value(),
                        clientResponse -> Mono.error(ClientError.unprocessableEntity()))
                .onStatus(httpStatus -> httpStatus.value() == HttpStatus.UNAUTHORIZED.value(),
                        clientResponse -> Mono.error(ClientError.unAuthorized()))
                .onStatus(HttpStatus::is5xxServerError,
                        clientResponse -> Mono.error(ClientError.networkServiceCallFailed()))
                .toBodilessEntity()
                .then();
    }

    @AllArgsConstructor
    @Value
    private static class BridgeRequest {
        String id;
        String url;
    }

    @AllArgsConstructor
    @Value
    private static class SessionRequest {
        String username;
        String password;
    }
}
