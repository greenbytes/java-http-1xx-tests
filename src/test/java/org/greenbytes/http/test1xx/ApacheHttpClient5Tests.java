package org.greenbytes.http.test1xx;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.apache.hc.client5.http.impl.async.MinimalHttpAsyncClient;
import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.config.Http1Config;
import org.apache.hc.core5.http.message.BasicHttpRequest;
import org.apache.hc.core5.http.nio.AsyncClientExchangeHandler;
import org.apache.hc.core5.http.nio.CapacityChannel;
import org.apache.hc.core5.http.nio.DataStreamChannel;
import org.apache.hc.core5.http.nio.RequestChannel;
import org.apache.hc.core5.http.nio.support.BasicRequestProducer;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.http.support.BasicRequestBuilder;
import org.apache.hc.core5.http2.HttpVersionPolicy;
import org.apache.hc.core5.http2.config.H2Config;
import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.reactor.IOReactorConfig;
import org.apache.hc.core5.util.Timeout;
import org.junit.Assert;
import org.junit.Test;

public class ApacheHttpClient5Tests extends TestHttp1xx {

    private void testApacheHttpClient5(Thread server, int expectedInformalStatus) throws IOException, InterruptedException, ParseException, ExecutionException {
        try {
            final IOReactorConfig ioReactorConfig = IOReactorConfig.custom().setSoTimeout(Timeout.ofSeconds(5)).build();

            final MinimalHttpAsyncClient client = HttpAsyncClients.createMinimal(HttpVersionPolicy.NEGOTIATE, H2Config.DEFAULT,
                    Http1Config.DEFAULT, ioReactorConfig);

            client.start();

            final BasicHttpRequest request = BasicRequestBuilder.get(TESTURI).build();
            final BasicRequestProducer requestProducer = new BasicRequestProducer(request, null);

            final AtomicInteger informationalStatus = new AtomicInteger();
            final AtomicInteger finalStatus = new AtomicInteger();
            final ByteArrayOutputStream payload = new ByteArrayOutputStream();

            final CountDownLatch latch = new CountDownLatch(1);
            client.execute(new AsyncClientExchangeHandler() {

                @Override
                public void releaseResources() {
                    requestProducer.releaseResources();
                    latch.countDown();
                }

                @Override
                public void cancel() {
                }

                @Override
                public void failed(final Exception cause) {
                }

                @Override
                public void produceRequest(final RequestChannel channel, final HttpContext context)
                        throws HttpException, IOException {
                    requestProducer.sendRequest(channel, context);
                }

                @Override
                public int available() {
                    return requestProducer.available();
                }

                @Override
                public void produce(final DataStreamChannel channel) throws IOException {
                    requestProducer.produce(channel);
                }

                @Override
                public void consumeInformation(final HttpResponse response, final HttpContext context)
                        throws HttpException, IOException {
                    informationalStatus.set(response.getCode());
                }

                @Override
                public void consumeResponse(final HttpResponse response, final EntityDetails entityDetails,
                        final HttpContext context) throws HttpException, IOException {
                    finalStatus.set(response.getCode());
                }

                @Override
                public void updateCapacity(final CapacityChannel capacityChannel) throws IOException {
                }

                @Override
                public void consume(final ByteBuffer src) throws IOException {
                    byte[] b = new byte[src.remaining()];
                    src.get(b, 0, b.length);
                    payload.write(b);
                }

                @Override
                public void streamEnd(final List<? extends Header> trailers) throws HttpException, IOException {
                }

            });
            latch.await(1, TimeUnit.MINUTES);

            client.close(CloseMode.GRACEFUL);

            int istatus = informationalStatus.get();
            int status = finalStatus.get();

            payload.flush();
            String body = new String(payload.toByteArray());

            if (istatus != 0) {
                System.err.println("C: istatus: " + istatus);
            }
            System.err.println("C: status: " + status);
            System.err.println("C: body: " + escapeLineEnds(body));

            if (expectedInformalStatus != -1) {
                Assert.assertEquals(expectedInformalStatus, istatus);
            }

            Assert.assertEquals(CONTENT, body);
            Assert.assertEquals(200, status);
        } finally {
            server.join();
        }
    }

    @Test
    public void testApacheHttpClient5200() throws IOException, InterruptedException, ParseException, ExecutionException {
        testApacheHttpClient5(create200Server(), -1);
    }

    @Test
    public void testApacheHttpClient5100() throws IOException, InterruptedException, ParseException, ExecutionException {
        testApacheHttpClient5(create100Server(), -1);
    }

    @Test
    public void testApacheHttpClient5102() throws IOException, InterruptedException, ParseException, ExecutionException {
        testApacheHttpClient5(create102Server(), 102);
    }

    @Test
    public void testApacheHttpClient5103() throws IOException, InterruptedException, ParseException, ExecutionException {
        testApacheHttpClient5(create103Server(), 103);
    }

    @Test
    public void testApacheHttpClient5199() throws IOException, InterruptedException, ParseException, ExecutionException {
        testApacheHttpClient5(create199Server(), 199);
    }
}
