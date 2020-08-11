package in.projecteka.devservice.common.heartbeat;

import in.projecteka.devservice.common.heartbeat.model.HeartbeatResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import static in.projecteka.devservice.common.Constants.PATH_HEARTBEAT;
import static in.projecteka.devservice.common.heartbeat.model.Status.UP;
import static java.time.LocalDateTime.now;
import static java.time.ZoneOffset.UTC;
import static org.springframework.http.HttpStatus.OK;
import static reactor.core.publisher.Mono.just;

@RestController
@AllArgsConstructor
public class HeartbeatController {

    @GetMapping(PATH_HEARTBEAT)
    public Mono<ResponseEntity<HeartbeatResponse>> getProvidersByName() {
        var heartbeatResponse = HeartbeatResponse.builder().timeStamp(now(UTC)).status(UP).build();
        return just(new ResponseEntity<>(heartbeatResponse, OK));
    }
}
