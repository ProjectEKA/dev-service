package in.projecteka.devservice.clients;

import in.projecteka.devservice.clients.model.Error;
import in.projecteka.devservice.clients.model.ErrorRepresentation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.web.ResourceProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ReactiveHttpOutputMessage;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Map;

import static in.projecteka.devservice.clients.model.ErrorCode.INVALID_REQUEST;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.BodyInserters.fromValue;

@Slf4j
public class ClientErrorExceptionHandler extends AbstractErrorWebExceptionHandler {
    public ClientErrorExceptionHandler(
            ErrorAttributes errorAttributes,
            ResourceProperties resourceProperties,
            ApplicationContext applicationContext) {
        super(errorAttributes, resourceProperties, applicationContext);
    }

    @Override
    protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
        return RouterFunctions.route(RequestPredicates.all(), this::renderErrorResponse);
    }

    private Mono<ServerResponse> renderErrorResponse(ServerRequest request) {
        Map<String, Object> errorPropertiesMap = getErrorAttributes(request, false);
        Throwable error = getError(request);
        // Default error response
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        BodyInserter<Object, ReactiveHttpOutputMessage> bodyInserter = BodyInserters.fromValue(errorPropertiesMap);

        if (error instanceof WebExchangeBindException) {
            WebExchangeBindException bindException = (WebExchangeBindException) error;
            FieldError fieldError = bindException.getFieldError();
            if (fieldError != null) {
                String errorMsg = fieldError.getField() + ": " + fieldError.getDefaultMessage();
                ErrorRepresentation errorRepresentation = ErrorRepresentation.builder()
                        .error(new Error(INVALID_REQUEST, errorMsg))
                        .build();
                bodyInserter = fromValue(errorRepresentation);
                return ServerResponse.status(BAD_REQUEST).contentType(APPLICATION_JSON).body(bodyInserter);
            }
        }

        if (error instanceof ClientError) {
            status = ((ClientError) error).getHttpStatus();
            bodyInserter = BodyInserters.fromValue(((ClientError) error).getError());
        }

        return ServerResponse.status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .body(bodyInserter);
    }
}
