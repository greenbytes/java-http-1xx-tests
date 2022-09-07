package org.greenbytes.http.test1xx;

import java.io.IOException;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;

public class OkHttpClientTests extends TestHttp1xx {
    
    private void testOkHttpClient(Thread server) throws IOException, InterruptedException {
        try {
            OkHttpClient client = new OkHttpClient.Builder().protocols(List.of(Protocol.HTTP_1_1)).build();
            Request request = new Request.Builder().url(TESTURI).get().build();
            try (Response response = client.newCall(request).execute()) {

                int status = response.code();
                String body = response.body().string();

                System.err.println("C: status: " + status);
                System.err.println("C: body: " + escapeLineEnds(body));

                Assert.assertEquals(CONTENT, body);
                Assert.assertEquals(200, status);
            }
        } finally {
            server.join();
        }
    }

    @Test
    public void testOkHttpClient100() throws IOException, InterruptedException {
        testOkHttpClient(create100Server());
    }

    @Test
    public void testOkHttpClient102() throws IOException, InterruptedException {
        testOkHttpClient(create102Server());
    }

    @Test
    public void testOkHttpClient103() throws IOException, InterruptedException {
        testOkHttpClient(create103Server());
    }

    @Test
    public void testOkHttpClient199() throws IOException, InterruptedException {
        testOkHttpClient(create199Server());
    }

    @Test
    public void testOkHttpClient200() throws IOException, InterruptedException {
        testOkHttpClient(create200Server());
    }
}
