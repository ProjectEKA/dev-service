package in.projecteka.devservice.support.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@AllArgsConstructor
public class SupportRequest {
    private final String name;
    private final String emailId;
    private final String phoneNumber;
    private final String organizationName;
    private final String expectedRoles;
    private final String status;
}
