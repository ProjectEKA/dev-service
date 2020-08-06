package in.projecteka.devservice.bridge;

import in.projecteka.devservice.bridge.model.BridgeRequest;
import in.projecteka.devservice.clients.ServiceAuthenticationClient;
import in.projecteka.devservice.clients.properties.GatewayServiceProperties;
import lombok.AllArgsConstructor;
import reactor.core.publisher.Mono;

@AllArgsConstructor
public class BridgeService {
    private final ServiceAuthenticationClient serviceAuthenticationClient;
    private final GatewayServiceProperties properties;

    public Mono<Void> updateBridgeUrl(String bridgeId, BridgeRequest bridgeRequest) {
        return serviceAuthenticationClient.getTokenFor(properties.getUsername(), properties.getPassword())
                .flatMap(session -> serviceAuthenticationClient.updateBridgeWith(bridgeId,
                        bridgeRequest.getUrl(), session.getAccessToken(), properties.getBaseUrl()));
    }
}
