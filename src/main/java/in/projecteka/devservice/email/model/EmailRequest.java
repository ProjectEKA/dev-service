package in.projecteka.devservice.email.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class EmailRequest {
    private String name;
    private String email;
    private String envDetails;
    private String repOrg;
    private String serveOrgId;
    private String endPoint;
    private String intent;
}
