package org.greenbytes.http.test1xx;

import java.io.IOException;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.junit.Assert;
import org.junit.Test;

public class ApacheHttpClient4Tests extends TestHttp1xx {

    private void testApacheHttpClient4(Thread server) throws IOException, InterruptedException {
        try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
            CloseableHttpResponse response = client.execute(new HttpGet(TESTURI));

            int status = response.getStatusLine().getStatusCode();
            String content = EntityUtils.toString(response.getEntity());

            logStatus(status, response.getStatusLine().getReasonPhrase());
            logContent(content);

            Assert.assertEquals(200, status);
            Assert.assertEquals(CONTENT, content);
        } finally {
            server.join();
        }
    }

    @Test
    public void testApacheHttpClient4100() throws IOException, InterruptedException {
        testApacheHttpClient4(create100Server());
    }

    @Test
    public void testApacheHttpClient4102() throws IOException, InterruptedException {
        testApacheHttpClient4(create102Server());
    }

    @Test
    public void testApacheHttpClient4103() throws IOException, InterruptedException {
        testApacheHttpClient4(create103Server());
    }

    @Test
    public void testApacheHttpClient4104_1() throws IOException, InterruptedException {
        testApacheHttpClient4(create104Server(1));
    }

    @Test
    public void testApacheHttpClient4104_2() throws IOException, InterruptedException {
        testApacheHttpClient4(create104Server(2));
    }

    @Test
    public void testApacheHttpClient4104_100() throws IOException, InterruptedException {
        testApacheHttpClient4(create104Server(100));
    }

    @Test
    public void testApacheHttpClient4199() throws IOException, InterruptedException {
        testApacheHttpClient4(create199Server());
    }

    @Test
    public void testApacheHttpClient4200() throws IOException, InterruptedException {
        testApacheHttpClient4(create200Server());
    }
}
