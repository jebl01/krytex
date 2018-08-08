package jebl01.krytex.handlers;

import io.vertx.ext.web.RoutingContext;
import jebl01.krytex.model.ServiceRepository;

public class DeleteHandler extends ServiceHandler {
    public DeleteHandler(final ServiceRepository repository) {
        super(repository);
    }

    @Override
    public void handle(final RoutingContext routingContext) {
        final String id = routingContext.request().getParam("id");
        repository.delete(id).setHandler(ar -> {
           if(ar.failed()) {
               routingContext.fail(ar.cause());
           } else {
               routingContext.response()
                   .setStatusCode(204)
                   .end();
           }
        });
    }
}
