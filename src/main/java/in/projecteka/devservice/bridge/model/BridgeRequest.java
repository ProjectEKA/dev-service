package in.projecteka.devservice.bridge.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@AllArgsConstructor
@Builder
@NoArgsConstructor
@Setter
public class BridgeRequest {
    @NotBlank(message = "url is not specified")
    String url;
}