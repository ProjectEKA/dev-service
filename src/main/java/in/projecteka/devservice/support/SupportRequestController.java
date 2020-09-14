package in.projecteka.devservice.support;

import in.projecteka.devservice.support.model.ApprovedRequestsSheet;
import in.projecteka.devservice.support.model.CredentialRequest;
import in.projecteka.devservice.support.model.SupportBridgeResponse;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import javax.validation.Valid;

import static in.projecteka.devservice.common.Constants.INTERNAL_GENERATE_ID_AND_SECRET;
import static in.projecteka.devservice.common.Constants.PATH_SUPPORT_REQUEST;


@AllArgsConstructor
@RestController
public class SupportRequestController {
    private final SupportRequestService supportRequestService;

    @PostMapping(PATH_SUPPORT_REQUEST)
    public Mono<Void> supportRequestEntry(@Valid @RequestBody ApprovedRequestsSheet approvedRequestsSheet) {
        return supportRequestService.processRequest(approvedRequestsSheet);
    }

    @PostMapping(INTERNAL_GENERATE_ID_AND_SECRET)
    public Mono<SupportBridgeResponse> getKeyAndSecret(@RequestBody CredentialRequest credentialRequest) {
        return supportRequestService.generateIdAndSecret(credentialRequest);
    }
}
