package in.projecteka.devservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.services.sheets.v4.SheetsScopes;
import in.projecteka.devservice.bridge.BridgeService;
import in.projecteka.devservice.clients.ClientErrorExceptionHandler;
import in.projecteka.devservice.clients.ClientRegistryClient;
import in.projecteka.devservice.clients.ServiceAuthenticationClient;
import in.projecteka.devservice.clients.properties.ClientRegistryProperties;
import in.projecteka.devservice.clients.properties.GatewayServiceProperties;
import in.projecteka.devservice.common.DbOptions;
import in.projecteka.devservice.email.EmailProperties;
import in.projecteka.devservice.email.EmailService;
import in.projecteka.devservice.email.GoogleServiceProperties;
import in.projecteka.devservice.support.SupportRequestRepository;
import in.projecteka.devservice.support.SupportRequestService;
import in.projecteka.devservice.support.model.SupportRequestProperties;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.PoolOptions;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

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

    @SneakyThrows
    @ConditionalOnProperty(value = "devservice.googleservice.enabled", havingValue = "true")
    @Bean({"credential"})
    public Credential credential(GoogleServiceProperties googleServiceProperties) {
        List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS);
        return GoogleCredential.fromStream(new FileInputStream(googleServiceProperties.getCredentialPath()))
                .createScoped(SCOPES);
    }

    @SneakyThrows
    @ConditionalOnProperty(value = "devservice.googleservice.enabled", havingValue = "false", matchIfMissing = true)
    @Bean({"credential"})
    public Credential credentialDisabled() {
        return new GoogleCredential();
    }

    @ConditionalOnProperty(value = "devservice.email.autoResponseEnabled", havingValue = "true")
    @Bean("autoResponseEmailBody")
    public String autoResponseEmailBody(EmailProperties emailProperties) throws IOException {
        return Files.readString(Paths.get(emailProperties.getAutoResponseBodyPath()));
    }

    @ConditionalOnProperty(value = "devservice.email.autoResponseEnabled", havingValue = "false", matchIfMissing = true)
    @Bean("autoResponseEmailBody")
    public String autoResponseEmptyBody() {
        return "";
    }

    @Bean
    public EmailService emailService(@Autowired JavaMailSender javaMailSender,
                                     EmailProperties emailProperties,
                                     GoogleServiceProperties googleServiceProperties,
                                     @Qualifier("credential") Credential credential,
                                     @Qualifier("autoResponseEmailBody") String autoResponseEmailBody) {
        return new EmailService(javaMailSender, emailProperties, googleServiceProperties, credential, autoResponseEmailBody);
    }

    @Bean
    public SupportRequestService supportRequestService(@Qualifier("credential") Credential credential,
                                                       SupportRequestRepository supportRequestRepository,
                                                       SupportRequestProperties supportRequestProperties,
                                                       ServiceAuthenticationClient serviceAuthenticationClient,
                                                       GatewayServiceProperties gatewayServiceProperties) {
        return new SupportRequestService(credential, supportRequestRepository, supportRequestProperties,
                serviceAuthenticationClient, gatewayServiceProperties);
    }

    @Bean("pgPoolClient")
    public PgPool pgPoolClient(DbOptions dbOptions) {
        PgConnectOptions connectOptions = new PgConnectOptions()
                .setPort(dbOptions.getPort())
                .setHost(dbOptions.getHost())
                .setDatabase(dbOptions.getSchema())
                .setUser(dbOptions.getUser())
                .setPassword(dbOptions.getPassword());

        PoolOptions poolOptions = new PoolOptions().setMaxSize(dbOptions.getPoolSize());
        return PgPool.pool(connectOptions, poolOptions);
    }
    @Bean
    public SupportRequestRepository supportRequestRepository(@Qualifier("pgPoolClient") PgPool pgPoolClient) {
        return new SupportRequestRepository(pgPoolClient);
    }
}
