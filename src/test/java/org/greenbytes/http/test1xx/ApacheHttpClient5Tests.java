package org.greenbytes.http.test1xx;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.hc.client5.http.async.methods.SimpleHttpRequest;
import org.apache.hc.client5.http.async.methods.SimpleHttpResponse;
import org.apache.hc.client5.http.async.methods.SimpleRequestBuilder;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.apache.hc.core5.http.ParseException;
import org.junit.Assert;
import org.junit.Test;

public class ApacheHttpClient5Tests extends TestHttp1xx {

    private void testApacheHttpClient5(Thread server) throws IOException, InterruptedException, ParseException, ExecutionException {
        try {
            CloseableHttpAsyncClient client = HttpAsyncClients.createDefault();
            client.start();

            SimpleHttpRequest request = SimpleRequestBuilder.get(TESTURI).build();
            Future<SimpleHttpResponse> future = client.execute(request, null);
            SimpleHttpResponse response = future.get();

            int status = response.getCode();
            String body = response.getBodyText();

            System.err.println("C: status: " + status);
            System.err.println("C: body: " + escapeLineEnds(body));

            Assert.assertEquals(CONTENT, body);
            Assert.assertEquals(200, status);
        } finally {
            server.join();
        }
    }

    @Test
    public void testApacheHttpClient5200() throws IOException, InterruptedException, ParseException, ExecutionException {
        testApacheHttpClient5(create200Server());
    }

    @Test
    public void testApacheHttpClient5100() throws IOException, InterruptedException, ParseException, ExecutionException {
        testApacheHttpClient5(create100Server());
    }

    @Test
    public void testApacheHttpClient5102() throws IOException, InterruptedException, ParseException, ExecutionException {
        testApacheHttpClient5(create102Server());
    }

    @Test
    public void testApacheHttpClient5103() throws IOException, InterruptedException, ParseException, ExecutionException {
        testApacheHttpClient5(create103Server());
    }

    @Test
    public void testApacheHttpClient5199() throws IOException, InterruptedException, ParseException, ExecutionException {
        testApacheHttpClient5(create199Server());
    }
}
