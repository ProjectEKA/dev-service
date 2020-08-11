package in.projecteka.devservice.common.heartbeat.model;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Builder
@Value
public class HeartbeatResponse {
    LocalDateTime timeStamp;
    Status status;
    Error error;
}
