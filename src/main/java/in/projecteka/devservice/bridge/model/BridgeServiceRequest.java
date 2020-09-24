package in.projecteka.devservice.bridge.model;

import lombok.Builder;
import lombok.Value;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Value
@Builder
public class BridgeServiceRequest {
    @NotBlank(message = "service id can't be blank")
    String id;
    @NotBlank(message = "service name can't be blank")
    String name;
    @NotNull(message = "service name alias can't be empty")
    List<String> alias;
    String city;
    @NotBlank(message = "service type can't be empty")
    ServiceType type;
    boolean active;
}
