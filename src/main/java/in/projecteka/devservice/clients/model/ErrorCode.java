package in.projecteka.devservice.clients.model;

public enum ErrorCode {
    INVALID_TOKEN(1401),
    BAD_REQUEST_FROM_GATEWAY(1510),
    NETWORK_SERVICE_ERROR(1511),
    INVALID_REQUEST(1512);

    private final int value;

    ErrorCode(int val) {
        value = val;
    }
}
