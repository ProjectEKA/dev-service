package in.projecteka.devservice.clients.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

public enum ErrorCode {
    INVALID_TOKEN(1401),
    BAD_REQUEST_FROM_GATEWAY(1510),
    NETWORK_SERVICE_ERROR(1511),
    EMAIL_SERVICE_ERROR(1511),
    INVALID_REQUEST(1512),
    DB_OPERATION_FAILED(1513),
    NO_SHEET_FOUND(1514),
    UNKNOWN_ERROR_OCCURRED(1515);
    private final int value;

    ErrorCode(int val) {
        value = val;
    }

    @JsonValue
    public int getValue() {
        return value;
    }

    @JsonCreator
    public static ErrorCode getNameByValue(int value) {
        return Arrays.stream(ErrorCode.values())
                .filter(errorCode -> errorCode.value == value)
                .findAny()
                .orElse(ErrorCode.UNKNOWN_ERROR_OCCURRED);
    }
}
