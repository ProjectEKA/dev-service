package in.projecteka.devservice.clients;

import in.projecteka.devservice.clients.model.Error;
import in.projecteka.devservice.clients.model.ErrorRepresentation;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import static in.projecteka.devservice.clients.model.ErrorCode.BAD_REQUEST_FROM_GATEWAY;
import static in.projecteka.devservice.clients.model.ErrorCode.EMAIL_SERVICE_ERROR;
import static in.projecteka.devservice.clients.model.ErrorCode.INVALID_TOKEN;
import static in.projecteka.devservice.clients.model.ErrorCode.NETWORK_SERVICE_ERROR;
import static in.projecteka.devservice.clients.model.ErrorCode.READ_SPREADSHEET_ERROR;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Getter
public class ClientError extends Throwable {
    private static final String CANNOT_PROCESS_REQUEST_TRY_LATER =
            "Cannot process the request at the moment, please try later.";
    private final HttpStatus httpStatus;
    private final ErrorRepresentation error;

    public ClientError(HttpStatus httpStatus, ErrorRepresentation errorRepresentation) {
        this.httpStatus = httpStatus;
        error = errorRepresentation;
    }

    public static ClientError unAuthorized() {
        return new ClientError(UNAUTHORIZED,
                new ErrorRepresentation(new Error(INVALID_TOKEN, "Token verification failed")));
    }

    public static ClientError networkServiceCallFailed() {
        return new ClientError(INTERNAL_SERVER_ERROR,
                new ErrorRepresentation(new Error(NETWORK_SERVICE_ERROR, CANNOT_PROCESS_REQUEST_TRY_LATER)));
    }

    public static ClientError emailSendingFailed() {
        return new ClientError(INTERNAL_SERVER_ERROR,
                new ErrorRepresentation(new Error(EMAIL_SERVICE_ERROR, CANNOT_PROCESS_REQUEST_TRY_LATER)));
    }

    public static ClientError unprocessableEntity() {
        return new ClientError(BAD_REQUEST,
                new ErrorRepresentation(new Error(BAD_REQUEST_FROM_GATEWAY, "Bad Request")));

    }

    public static ClientError spreadsheetReadingFailed() {
        return new ClientError(BAD_REQUEST,
                new ErrorRepresentation(new Error(READ_SPREADSHEET_ERROR, "Cannot read the sheet")));
    }
    public static ClientError invalidServiceType() {
        return new ClientError(BAD_REQUEST,
                new ErrorRepresentation(new Error(BAD_REQUEST_FROM_GATEWAY, "Invalid service type")));

    }



}
