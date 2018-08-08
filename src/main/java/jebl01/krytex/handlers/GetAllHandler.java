package jebl01.krytex.handlers;

import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;
import jebl01.krytex.model.ServiceRepository;

public class GetAllHandler extends ServiceHandler {
    public GetAllHandler(final ServiceRepository repository) {
        super(repository);
    }

    @Override
    public void handle(final RoutingContext routingContext) {
        this.repository.getServices().setHandler(ar -> {
           if(ar.failed()) {
               routingContext.fail(ar.cause());
           } else {
               routingContext.response()
                   .putHeader("content-type", CONTENT_TYPE_JSON_UTF8)
                   .end(Json.encode(ar.result()));
           }
        });
    }
}
