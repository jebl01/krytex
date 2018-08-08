package jebl01.krytex.model;

import static jebl01.krytex.MainVerticle.DATE_FORMAT;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.Json;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

import java.text.ParseException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class ServiceRepositoryTest {
    private Vertx vertx;

    @Before
    public void setUp(TestContext context) {
        VertxOptions options = new VertxOptions();
        options.setWorkerPoolSize(3);
        options.setInternalBlockingPoolSize(3);
        options.setEventLoopPoolSize(3);

        vertx = Vertx.vertx(options);

        Json.mapper.setDateFormat(DATE_FORMAT);
    }

    @After
    public void tearDown(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

    @Test
    public void fullTest(final TestContext context) throws ParseException, InterruptedException {
        System.out.println(getTempFile());

        final List<Service> initialServices = Arrays.asList(
            servicesFixture("id1", "test1"),
            servicesFixture("id2", "test2"));


        final CountDownLatch countDownLatch = new CountDownLatch(1);
        final ServiceRepository serviceRepository = new FileRepository(vertx, getTempFile());

        //first persist two services
        serviceRepository.persistAll(initialServices)
            .compose(v -> serviceRepository.getServices())
            .compose(services -> {
                context.assertEquals(2, services.size());

                final Service first = services.get(0);
                final Service second = services.get(1);

                try {
                    context.assertEquals("id1", first.id);
                    context.assertEquals("test1", first.name);
                    context.assertEquals("www.test.com", first.url);
                    context.assertEquals(Status.OK, first.status);
                    context.assertEquals(DATE_FORMAT.parse("2014-06-16 05:42"), first.lastCheck);

                    context.assertEquals("id2", second.id);
                    context.assertEquals("test2", second.name);
                    context.assertEquals("www.test.com", second.url);
                    context.assertEquals(Status.OK, second.status);
                    context.assertEquals(DATE_FORMAT.parse("2014-06-16 05:42"), second.lastCheck);
                } catch(Exception ex) {
                    context.fail(ex);
                }

                //add a third service
                return serviceRepository.persist(servicesFixture("id3", "test3"));
            }).compose(service -> {
                context.assertEquals("id3", service.id);
                return serviceRepository.getServices();
        })
            //validate we have three services
            .compose(services -> {
            context.assertEquals(3, services.size());

            final Service first = services.get(0);
            final Service second = services.get(1);
            final Service third = services.get(2);

            try {
                context.assertEquals("id1", first.id);
                context.assertEquals("test1", first.name);
                context.assertEquals("www.test.com", first.url);
                context.assertEquals(Status.OK, first.status);
                context.assertEquals(DATE_FORMAT.parse("2014-06-16 05:42"), first.lastCheck);

                context.assertEquals("id2", second.id);
                context.assertEquals("test2", second.name);
                context.assertEquals("www.test.com", second.url);
                context.assertEquals(Status.OK, second.status);
                context.assertEquals(DATE_FORMAT.parse("2014-06-16 05:42"), second.lastCheck);

                context.assertEquals("id3", third.id);
                context.assertEquals("test3", third.name);
                context.assertEquals("www.test.com", third.url);
                context.assertEquals(Status.OK, third.status);
                context.assertEquals(DATE_FORMAT.parse("2014-06-16 05:42"), third.lastCheck);
            } catch(Exception ex) {
                context.fail(ex);
            }

            //delete one service
            return serviceRepository.delete("id1");
        })
            //validate that the service was removed
            .compose(v -> serviceRepository.getServices())
            .compose(services -> {
                context.assertEquals(2, services.size());

                final Service first = services.get(0);
                final Service second = services.get(1);

                try {
                    context.assertEquals("id2", first.id);
                    context.assertEquals("test2", first.name);
                    context.assertEquals("www.test.com", first.url);
                    context.assertEquals(Status.OK, first.status);
                    context.assertEquals(DATE_FORMAT.parse("2014-06-16 05:42"), first.lastCheck);

                    context.assertEquals("id3", second.id);
                    context.assertEquals("test3", second.name);
                    context.assertEquals("www.test.com", second.url);
                    context.assertEquals(Status.OK, second.status);
                    context.assertEquals(DATE_FORMAT.parse("2014-06-16 05:42"), second.lastCheck);
                } catch(Exception ex) {
                    context.fail(ex);
                }
                return Future.succeededFuture();
            }).setHandler(ar -> {
            if(ar.failed()) {
                context.fail(ar.cause());
            } else {
                System.out.println("success!");
            }
            countDownLatch.countDown();
        });

        if(!countDownLatch.await(3, TimeUnit.SECONDS)) {
            context.fail("timeout waiting for countdown latch!");
        }
    }


    private String getTempFile() {
        return System.getProperty("java.io.tmpdir") + UUID.randomUUID().toString() + ".json";
    }

    private Service servicesFixture(final String id, final String name) {
        try {
            return new Service(id,name, "www.test.com", Status.OK, DATE_FORMAT.parse("2014-06-16 05:42"));
        }
        catch(ParseException e) {
            throw new RuntimeException("failed to parse date!");
        }
    }
}
