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
            String content = readFully(c.getInputStream());

            logStatus(status, c.getResponseMessage());
            logContent(content);

            Assert.assertEquals(200, status);
            Assert.assertEquals(CONTENT, content);
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
    public void testHTTPURLConnection104_1() throws IOException, InterruptedException {
        testHTTPURLConnection(create104Server(1));
    }

    @Test
    public void testHTTPURLConnection104_2() throws IOException, InterruptedException {
        testHTTPURLConnection(create104Server(2));
    }

    @Test
    public void testHTTPURLConnection104_100() throws IOException, InterruptedException {
        testHTTPURLConnection(create104Server(100));
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
