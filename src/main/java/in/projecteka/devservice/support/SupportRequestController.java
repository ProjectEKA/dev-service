package in.projecteka.devservice.support;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.security.GeneralSecurityException;

import static in.projecteka.devservice.common.Constants.PATH_SUPPORT_REQUEST;


@AllArgsConstructor
@RestController
public class SupportRequestController {
    private final SupportRequestService supportRequestService;

    @PostMapping(PATH_SUPPORT_REQUEST)
    public Mono<Void> bridgeEntry() throws GeneralSecurityException, IOException {
     return supportRequestService.processRequest();
    }
}
