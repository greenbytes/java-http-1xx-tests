package org.greenbytes.http.test1xx;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

import org.junit.Assert;
import org.junit.Test;

public class JavaNetHttpHttpClientTests extends TestHttp1xx {

    private void testJavaNetHttpHttpClient(Thread server) throws IOException, InterruptedException {
        try {
            HttpClient client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build();
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(TESTURI)).GET().build();
            HttpResponse<String> response = client.send(request, BodyHandlers.ofString());

            int status = response.statusCode();
            String content = response.body();

            logStatus(status, null);
            logContent(content);

            Assert.assertEquals(200, status);
            Assert.assertEquals(CONTENT, content);
        } finally {
            server.join();
        }
    }

    @Test
    public void testJDKHttpClient100() throws IOException, InterruptedException {
        testJavaNetHttpHttpClient(create100Server());
    }

    @Test
    public void testJDKHttpClient102() throws IOException, InterruptedException {
        testJavaNetHttpHttpClient(create102Server());
    }

    @Test
    public void testJDKHttpClient103() throws IOException, InterruptedException {
        testJavaNetHttpHttpClient(create103Server());
    }

    @Test
    public void testJDKHttpClient104_1() throws IOException, InterruptedException {
        testJavaNetHttpHttpClient(create104Server(1));
    }

    @Test
    public void testJDKHttpClient104_2() throws IOException, InterruptedException {
        testJavaNetHttpHttpClient(create104Server(2));
    }

    @Test
    public void testJDKHttpClient104_100() throws IOException, InterruptedException {
        testJavaNetHttpHttpClient(create104Server(100));
    }

    @Test
    public void testJDKHttpClient199() throws IOException, InterruptedException {
        testJavaNetHttpHttpClient(create199Server());
    }

    @Test
    public void testJDKHttpClient200() throws IOException, InterruptedException {
        testJavaNetHttpHttpClient(create200Server());
    }
}
