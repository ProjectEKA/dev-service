package in.projecteka.devservice.support;


import in.projecteka.devservice.common.DbOperationError;
import in.projecteka.devservice.support.model.SupportRequest;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.Tuple;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import static reactor.core.publisher.Mono.create;

@AllArgsConstructor
public class SupportRequestRepository {
    private final PgPool pgPoolClient;
    private final static String INSERT_REQUEST = "Insert into support_requests(name, email_id, phone_number, organization_name, expected_roles, status, support_request_id)" +
            " values($1, $2, $3, $4, $5, $6, $7);";
    private final static String SELECT_SUPPORT_REQUEST = "Select name, email_id, phone_number, organization_name, expected_roles, status, support_request_id from support_requests where support_request_id=$1";
    private static final Logger logger = LoggerFactory.getLogger(SupportRequestRepository.class);


    private Mono<Void> doOperation(String query, Tuple parameters) {
        return create(monoSink -> pgPoolClient.preparedQuery(query)
                .execute(parameters, handler -> {
                    if (handler.failed()) {
                        monoSink.error(new DbOperationError());
                        return;
                    }
                    monoSink.success();
                }));
    }

    public Mono<Void> insert(SupportRequest supportRequest) {
        Tuple request = Tuple.of(
                supportRequest.getName(),
                supportRequest.getEmailId(),
                supportRequest.getPhoneNumber(),
                supportRequest.getOrganizationName(),
                supportRequest.getExpectedRoles(),
                supportRequest.getStatus(),
                supportRequest.getSupportRequestId());
        return doOperation(INSERT_REQUEST, request);
    }

    public Mono<SupportRequest> getSupportRequest(String requestId) {
        return create(monoSink -> pgPoolClient.preparedQuery(SELECT_SUPPORT_REQUEST)
                .execute(Tuple.of(requestId),
                        handler -> {
                            if (handler.failed()) {
                                logger.error(handler.cause().getMessage(), handler.cause());
                                monoSink.error(new DbOperationError());
                                return;
                            }
                            var requestIterator = handler.result().iterator();
                            if (!requestIterator.hasNext()) {
                                monoSink.success();
                                return;
                            }
                            var requestRow = requestIterator.next();
                            try {
                                var supportRequest = SupportRequest.builder()
                                        .name(requestRow.getString("name"))
                                        .organizationName(requestRow.getString("organization_name"))
                                        .status(requestRow.getString("status"))
                                        .expectedRoles(requestRow.getString("expected_roles"))
                                        .phoneNumber(requestRow.getString("phone_number"))
                                        .supportRequestId(requestRow.getString("support_request_id"))
                                        .emailId(requestRow.getString("email_id"))
                                        .build();
                                monoSink.success(supportRequest);
                            } catch (Exception exc) {
                                logger.error(exc.getMessage(), exc);
                                monoSink.success();
                            }
                        }));
    }
}
