package in.projecteka.devservice.bridge.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import in.projecteka.devservice.bridge.ServiceTypeDeserializer;

@JsonDeserialize(using = ServiceTypeDeserializer.class)
public enum ServiceType {
    HIP,
    HIU,
    INVALID_TYPE;

    public static ServiceType fromText(String serviceType) {
        if (serviceType.equals("HIP")
                || serviceType.equals("HIU")) {
            return ServiceType.valueOf(serviceType);
        } else {
            return ServiceType.INVALID_TYPE;
        }
    }
}
