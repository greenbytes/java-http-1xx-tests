package org.greenbytes.http.test1xx;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import org.junit.Assert;
import org.junit.Test;

public class JavaNetHTTPURLConnectionTests extends TestHttp1xx {

    private void testHTTPURLConnection(Thread server) throws IOException, InterruptedException {
        try {
            URL test = new URL(TESTURI);
            HttpURLConnection c = (HttpURLConnection) test.openConnection();

            int status = c.getResponseCode();
            String body = readFully(c.getInputStream());

            System.err.println("C: status: " + status);
            System.err.println("C: body: " + escapeLineEnds(body));

            Assert.assertEquals(CONTENT, body);
            Assert.assertEquals(200, status);
        } finally {
            server.join();
        }
    }

    @Test
    public void testHTTPURLConnection100() throws IOException, InterruptedException {
        testHTTPURLConnection(create100Server());
    }

    @Test
    public void testHTTPURLConnection102() throws IOException, InterruptedException {
        testHTTPURLConnection(create102Server());
    }

    @Test
    public void testHTTPURLConnection103() throws IOException, InterruptedException {
        testHTTPURLConnection(create103Server());
    }

    @Test
    public void testHTTPURLConnection199() throws IOException, InterruptedException {
        testHTTPURLConnection(create199Server());
    }

    @Test
    public void testHTTPURLConnection200() throws IOException, InterruptedException {
        testHTTPURLConnection(create200Server());
    }
}
