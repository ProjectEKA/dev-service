package in.projecteka.devservice.bridge.model;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class OrganizationDetails {
    String id;
    String name;
    String city;
    List<String> orgAlias;
}
