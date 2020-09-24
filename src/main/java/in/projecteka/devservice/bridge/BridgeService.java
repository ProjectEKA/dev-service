package in.projecteka.devservice.bridge;

import in.projecteka.devservice.bridge.model.BridgeRequest;
import in.projecteka.devservice.bridge.model.BridgeServiceRequest;
import in.projecteka.devservice.bridge.model.OrganizationDetails;
import in.projecteka.devservice.bridge.model.ServiceType;
import in.projecteka.devservice.clients.ClientError;
import in.projecteka.devservice.clients.ClientRegistryClient;
import in.projecteka.devservice.clients.ServiceAuthenticationClient;
import in.projecteka.devservice.clients.properties.GatewayServiceProperties;
import lombok.AllArgsConstructor;
import reactor.core.publisher.Mono;

@AllArgsConstructor
public class BridgeService {
    private final ServiceAuthenticationClient serviceAuthenticationClient;
    private final ClientRegistryClient clientRegistryClient;
    private final GatewayServiceProperties properties;

    public Mono<Void> updateBridgeUrl(String bridgeId, BridgeRequest bridgeRequest) {
        return serviceAuthenticationClient.getTokenFor(properties.getUsername(), properties.getPassword())
                .flatMap(session -> serviceAuthenticationClient.updateBridgeWith(bridgeId,
                        bridgeRequest.getUrl(), session.getTokenType() + " " + session.getAccessToken()));
    }

    public Mono<Void> upsertBridgeServiceEntry(String bridgeId, BridgeServiceRequest request) {
        return serviceAuthenticationClient.getTokenFor(properties.getUsername(), properties.getPassword())
                .flatMap(session -> {
                    if(request.getType().equals(ServiceType.INVALID_TYPE))
                        return Mono.error(ClientError.invalidServiceType());
                    OrganizationDetails orgDetails = OrganizationDetails.builder()
                            .id(request.getId())
                            .name(request.getName())
                            .orgAlias(request.getAlias())
                            .city(getCity(request))
                            .serviceType(request.getType())
                            .build();

                    return serviceAuthenticationClient.upsertBridgeServiceEntry(
                            bridgeId, request.getId(), request.getName(),
                            request.getType(), request.isActive(), session)
                            .then(clientRegistryClient.addOrganization(orgDetails));
                });
    }

    private String getCity(BridgeServiceRequest request) {
        return request.getCity() != null ? request.getCity() : "";
    }
}
