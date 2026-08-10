package org.greenbytes.http.test1xx;

import org.eclipse.jetty.client.ContentResponse;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.http.HttpVersion;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class JettyHttpClientTests extends TestHttp1xx {

    private void testJettyHttpClient(Thread server) throws Exception {
        HttpClient client = new HttpClient();
        client.start();
        try {
            ContentResponse response = client.newRequest(TESTURI)
                    .version(HttpVersion.HTTP_1_1)
                    .send();

            int status = response.getStatus();
            String content = response.getContentAsString();

            logStatus(status, response.getReason());
            logContent(content);

            assertEquals(200, status);
            assertEquals(CONTENT, content);
        } finally {
            client.stop();
            server.join();
        }
    }

    @Test
    public void testJettyHttpClient100() throws Exception {
        testJettyHttpClient(create100Server());
    }

    @Test
    public void testJettyHttpClient102() throws Exception {
        testJettyHttpClient(create102Server());
    }

    @Test
    public void testJettyHttpClient103() throws Exception {
        testJettyHttpClient(create103Server());
    }

    @Test
    public void testJettyHttpClient104_1() throws Exception {
        testJettyHttpClient(create104Server(1));
    }

    @Test
    public void testJettyHttpClient104_2() throws Exception {
        testJettyHttpClient(create104Server(2));
    }

    @Test
    public void testJettyHttpClient104_100() throws Exception {
        testJettyHttpClient(create104Server(100));
    }

    @Test
    public void testJettyHttpClient199() throws Exception {
        testJettyHttpClient(create199Server());
    }

    @Test
    public void testJettyHttpClient200() throws Exception {
        testJettyHttpClient(create200Server());
    }
}