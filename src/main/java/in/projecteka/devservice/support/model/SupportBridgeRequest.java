package in.projecteka.devservice.support.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class SupportBridgeRequest {
    String id;
    String name;
    String url;
    Boolean active;
    Boolean blocklisted;
}
