package in.projecteka.devservice.support;


import in.projecteka.devservice.common.DbOperationError;
import in.projecteka.devservice.support.model.SupportRequest;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.Tuple;
import lombok.AllArgsConstructor;
import reactor.core.publisher.Mono;

import static reactor.core.publisher.Mono.create;

@AllArgsConstructor
public class SupportRequestRepository {
    private final PgPool pgPoolClient;
    private final static String INSERT_REQUEST = "Insert into support_requests(name, email_id, phone_number, organization_name, expected_roles, status)" +
            " values($1, $2, $3, $4, $5, $6);";


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
               supportRequest.getStatus());
       return doOperation(INSERT_REQUEST, request);
    }
}
