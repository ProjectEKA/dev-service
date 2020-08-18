package in.projecteka.devservice.email;

import in.projecteka.devservice.clients.ClientError;
import in.projecteka.devservice.email.model.EmailRequest;
import lombok.AllArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import reactor.core.publisher.Mono;

@AllArgsConstructor
public class EmailService {

    private JavaMailSender javaMailSender;
    private EmailProperties emailProperties;

    public Mono<Void> sendEmail(EmailRequest emailRequest) {
        return Mono.create(monoSink -> {
            try {
                SimpleMailMessage msg = new SimpleMailMessage();
                msg.setTo(emailProperties.getReceiver());
                msg.setFrom(emailProperties.getSender());
                msg.setSubject(emailProperties.getSubject());
                msg.setText(String.format("Please find the details \n\n" +
                                "Name: %s \n" +
                                "Email address: %s \n" +
                                "Organization you represent: %s \n" +
                                "Organizations Ids you serve: %s\n" +
                                "Intent: %s\n" +
                                "Endpoint: %s \n\n" +
                                "Regards\n" +
                                "%s",
                        emailRequest.getName(),
                        emailRequest.getEmail(),
                        emailRequest.getRepOrg(),
                        emailRequest.getServeOrgId(),
                        emailRequest.getIntent(),
                        emailRequest.getEndPoint(),
                        emailProperties.getSender()));

                javaMailSender.send(msg);
                monoSink.success();
            } catch (Exception e) {
                monoSink.error(ClientError.networkServiceCallFailed());
            }
        });
    }
}
