package in.projecteka.devservice.support.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Value;

@Builder
@Data
@Value
@AllArgsConstructor
public class SupportRequest {
    private final String name;
    private final String emailId;
    private final String phoneNumber;
    private final String organizationName;
    private final String expectedRoles;
    private final String status;
    private final String supportRequestId;
}
