package jebl01.krytex.handlers;

import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;
import jebl01.krytex.model.Service;
import jebl01.krytex.model.ServiceRepository;
import jebl01.krytex.model.TransientService;

public class CreateHandler extends ServiceHandler {
    public CreateHandler(final ServiceRepository repository) {
        super(repository);
    }

    @Override
    public void handle(final RoutingContext routingContext) {
        final TransientService transientService = Json.decodeValue(routingContext.getBodyAsString(), TransientService.class);
        final Service service = Service.createNew(transientService);

        repository.persist(service).setHandler(ar -> {
            if(ar.failed()) {
                routingContext.fail(ar.cause());
            }
            else {
                routingContext.response()
                    .setStatusCode(201)
                    .putHeader("content-type", CONTENT_TYPE_JSON_UTF8)
                    .end(Json.encodePrettily(ar.result()));
            }
        });
    }
}
