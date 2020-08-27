package in.projecteka.devservice.email;

import in.projecteka.devservice.email.model.Field;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import javax.validation.Valid;

import java.util.List;

import static in.projecteka.devservice.common.Constants.PATH_EMAIL_SEND;

@AllArgsConstructor
@RestController
public class EmailController {
    private final EmailService emailService;

    @CrossOrigin(origins = "${devservice.email.allowedOrigin}")
    @PostMapping(PATH_EMAIL_SEND)
    public Mono<Void> bridgeEntry(@Valid @RequestBody List<Field> emailRequest) {
        return emailService.sendEmail(emailRequest);
    }
}
