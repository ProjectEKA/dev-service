package in.projecteka.devservice.bridge;

import in.projecteka.devservice.bridge.model.BridgeRequest;
import in.projecteka.devservice.common.Caller;
import lombok.AllArgsConstructor;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import static in.projecteka.devservice.common.Constants.PATH_BRIDGES;

@AllArgsConstructor
@RestController
public class BridgeController {
    private final BridgeService bridgeService;

    @PatchMapping(PATH_BRIDGES)
    public Mono<Void> bridgeEntry(@RequestBody BridgeRequest bridgeRequest) {
        return ReactiveSecurityContextHolder.getContext()
                .map(securityContext -> (Caller) securityContext.getAuthentication().getPrincipal())
                .flatMap(requester -> bridgeService.updateBridgeUrl(requester.getClientId(), bridgeRequest));
    }
}
