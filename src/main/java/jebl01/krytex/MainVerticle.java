package jebl01.krytex;

import io.vertx.config.ConfigRetriever;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import jebl01.krytex.handlers.CreateHandler;
import jebl01.krytex.handlers.DeleteHandler;
import jebl01.krytex.handlers.GetAllHandler;
import jebl01.krytex.model.FileRepository;
import jebl01.krytex.model.ServiceRepository;

import java.text.SimpleDateFormat;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class MainVerticle extends AbstractVerticle {
    public static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy hh:mm");

    static {
        Json.mapper.setDateFormat(DATE_FORMAT);
        System.setProperty("java.net.preferIPv4Stack", "true");
        java.security.Security.setProperty("networkaddress.cache.ttl", "60");
    }

    //only used for starting app in IDE
    public static void main(String[] args) throws InterruptedException, ExecutionException, TimeoutException {
        final CompletableFuture<AsyncResult<String>> resultFuture = new CompletableFuture<>();
        Vertx.vertx().deployVerticle(new MainVerticle(), resultFuture::complete);
        final AsyncResult<String> result = resultFuture.get(2, TimeUnit.SECONDS);
        if(result.failed()) {
            throw new RuntimeException(result.cause());
        }
    }

    @Override
    public void start(final Future<Void> future) throws Exception {
        final ConfigRetriever configRetriever = ConfigRetriever.create(vertx);
        final Router router = Router.router(vertx);

        //always require config
        configRetriever.getConfig(ar -> {
            if(ar.failed()) {
                future.fail(ar.cause());
            }
            else {
                //read config
                final int port = ar.result().getInteger("server.port", 8080);
                final String filePath = ar.result().getString("repository.path", "~/krytex.data");
                final int maxConcurrentPolls = ar.result().getInteger("maxConcurrentPolls", 3);
                final int pollIntervalMs = ar.result().getInteger("pollIntervalMs", 60_000);

                //wire stuff
                final ServiceRepository serviceRepository = new FileRepository(vertx, filePath);
                final ServicePoller servicePoller = new ServicePoller(
                    serviceRepository,
                    io.vertx.rxjava.core.Vertx.vertx().createHttpClient(),
                    maxConcurrentPolls);

                //wire routes
                router.get("/service").handler(new GetAllHandler(serviceRepository));
                router.route("/service").handler(BodyHandler.create());
                router.post("/service").handler(new CreateHandler(serviceRepository));
                router.delete("/service/:id").handler(new DeleteHandler(serviceRepository));

                //start server and poller
                vertx.createHttpServer()
                    .requestHandler(router::accept)
                    .listen(port, result -> {
                        if(result.succeeded()) {
                            System.out.println("HTTP server started on port: " + port);
                            System.out.println("File store located at: " + filePath);

                            //schedule poller!
                            System.out.println("scheduling poller, maxConcurrentPolls=" + maxConcurrentPolls + " pollIntervalMs=" + pollIntervalMs);
                            vertx.setPeriodic(pollIntervalMs, id -> servicePoller.poll());
                            future.complete();
                        }
                        else {
                            future.fail(result.cause());
                        }
                    });
            }
        });
    }
}
