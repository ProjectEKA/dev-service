package in.projecteka.devservice.bridge.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Endpoint {
    EndpointUse use;
    EndpointConnectionType connectionType;
    String address;
}
