package in.projecteka.devservice.bridge.model;

import lombok.Builder;
import lombok.Value;

import javax.validation.constraints.NotBlank;
import java.util.List;

@Value
@Builder
public class BridgeServiceRequest {
    @NotBlank(message = "hospital id can't be blank")
    String id;
    @NotBlank(message = "hospital name can't be blank")
    String name;
    @NotBlank(message = "hospital name alias can't be blank")
    List<String> alias;
    String city;
    @NotBlank(message = "Service type can be either HIP or HIU")
    ServiceType type;
    boolean active;
}
