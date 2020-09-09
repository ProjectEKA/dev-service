package in.projecteka.devservice.support.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@AllArgsConstructor
public class ApprovedRequest {
    private String sheetName;
}
