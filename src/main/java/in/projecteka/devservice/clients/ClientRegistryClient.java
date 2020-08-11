package in.projecteka.devservice.clients;

import in.projecteka.devservice.bridge.model.OrganizationDetails;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

public class ClientRegistryClient {
    private final WebClient webClient;

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
                        clientResponse -> Mono.error(ClientError.unprocessableEntity()))
                .onStatus(HttpStatus::isError, clientResponse -> Mono.error(ClientError.networkServiceCallFailed()))
                .toBodilessEntity()
                .then();
    }
}
