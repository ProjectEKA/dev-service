package in.projecteka.devservice.support.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@AllArgsConstructor
@Builder
@NoArgsConstructor
@Setter
public class ApprovedRequest {
    private String sheetName;
}
