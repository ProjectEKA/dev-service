package in.projecteka.devservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import in.projecteka.devservice.bridge.BridgeService;
import in.projecteka.devservice.clients.ClientErrorExceptionHandler;
import in.projecteka.devservice.clients.ClientRegistryClient;
import in.projecteka.devservice.clients.ServiceAuthenticationClient;
import in.projecteka.devservice.clients.properties.ClientRegistryProperties;
import in.projecteka.devservice.clients.properties.GatewayServiceProperties;
import in.projecteka.devservice.email.EmailProperties;
import in.projecteka.devservice.email.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.web.ResourceProperties;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class DevServiceConfiguration {
    @Bean("customBuilder")
    public WebClient.Builder webClient(final ClientHttpConnector clientHttpConnector, ObjectMapper objectMapper) {
        return WebClient
                .builder()
                .exchangeStrategies(exchangeStrategies(objectMapper))
                .clientConnector(clientHttpConnector);
    }

    private ExchangeStrategies exchangeStrategies(ObjectMapper objectMapper) {
        var encoder = new Jackson2JsonEncoder(objectMapper);
        var decoder = new Jackson2JsonDecoder(objectMapper);
        return ExchangeStrategies
                .builder()
                .codecs(configurer -> {
                    configurer.defaultCodecs().jackson2JsonEncoder(encoder);
                    configurer.defaultCodecs().jackson2JsonDecoder(decoder);
                }).build();
    }

    @Bean
    public ServiceAuthenticationClient serviceAuthenticationClient(
            @Qualifier("customBuilder") WebClient.Builder webClientBuilder,
            GatewayServiceProperties gatewayServiceProperties) {
        return new ServiceAuthenticationClient(webClientBuilder, gatewayServiceProperties.getBaseUrl());
    }

    @Bean
    public ClientRegistryClient clientRegistryClient(@Qualifier("customBuilder") WebClient.Builder builder,
                                                     ClientRegistryProperties clientRegistryProperties) {
        return new ClientRegistryClient(builder, clientRegistryProperties.getUrl());
    }

    @Bean
    public BridgeService bridgeService(ServiceAuthenticationClient serviceAuthenticationClient,
                                       GatewayServiceProperties gatewayServiceProperties,
                                       ClientRegistryClient clientRegistryClient) {
        return new BridgeService(serviceAuthenticationClient, clientRegistryClient, gatewayServiceProperties);
    }

    @Bean
    // This exception handler needs to be given highest priority compared to DefaultErrorWebExceptionHandler, hence order = -2.
    @Order(-2)
    public ClientErrorExceptionHandler clientErrorExceptionHandler(ErrorAttributes errorAttributes,
                                                                   ResourceProperties resourceProperties,
                                                                   ApplicationContext applicationContext,
                                                                   ServerCodecConfigurer serverCodecConfigurer) {

        ClientErrorExceptionHandler clientErrorExceptionHandler = new ClientErrorExceptionHandler(errorAttributes,
                resourceProperties, applicationContext);
        clientErrorExceptionHandler.setMessageWriters(serverCodecConfigurer.getWriters());
        return clientErrorExceptionHandler;
    }

    @Bean
    public EmailService emailService(@Autowired JavaMailSender javaMailSender, EmailProperties emailProperties){
        return new EmailService(javaMailSender, emailProperties);
    }
}
