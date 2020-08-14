package in.projecteka.devservice.clients;

import in.projecteka.devservice.bridge.model.OrganizationDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Properties;

import static in.projecteka.devservice.clients.ClientError.networkServiceCallFailed;
import static in.projecteka.devservice.clients.ClientError.unprocessableEntity;
import static reactor.core.publisher.Mono.error;

public class ClientRegistryClient {
    private final WebClient webClient;
    private final Logger logger = LoggerFactory.getLogger(ClientRegistryClient.class);


    public ClientRegistryClient(WebClient.Builder webClient, String baseUrl) {
        this.webClient = webClient.baseUrl(baseUrl).build();
    }

    public Mono<Void> addOrganization(OrganizationDetails organizationDetails) {
        return webClient
                .post()
                .uri("/api/2.0/organizations")
                .bodyValue(organizationDetails)
                .retrieve()
                .onStatus(httpStatus -> httpStatus.value() == HttpStatus.BAD_REQUEST.value(),
                        clientResponse -> clientResponse.bodyToMono(Properties.class)
                                .doOnNext(properties -> logger.error(properties.toString()))
                                .then(error(unprocessableEntity()))
                )
                .onStatus(HttpStatus::isError,
                        clientResponse -> clientResponse.bodyToMono(Properties.class)
                                .doOnNext(properties -> logger.error(properties.toString()))
                                .then(error(networkServiceCallFailed()))
                )
                .toBodilessEntity()
                .then();
    }
}
