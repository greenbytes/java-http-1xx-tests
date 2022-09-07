package org.greenbytes.http.test1xx;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.List;
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

import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;

public class TestHttp1xx {

    protected static String CONTENT = "Hello, world.";

    private static String CRLF = String.format("%c%c", 13, 10);
    private static String FINALMESSAGE = "HTTP/1.1 200 OK" + CRLF + "Content-Type: text/plain" + CRLF + "Content-Length: "
            + CONTENT.length() + CRLF + CRLF + CONTENT;
    private static int PORT = 8080;

    protected static String TESTURI = "http://localhost:" + PORT;
 
    private Thread createServer(int status, String reason, String fields) throws IOException {
        Runnable server = new Runnable() {
            @Override
            public void run() {
                ServerSocket serverSocket = null;
                try {
                    String response = "";
                    if (status >= 0) {
                        response += "HTTP/1.1 " + status + " " + reason + CRLF;
                        if (fields != null) {
                            response += fields;
                        }
                        response += CRLF;
                    }
                    response += FINALMESSAGE;

                    boolean up = false;
                    while (!up) {
                        try {
                            serverSocket = new ServerSocket(PORT);
                            up = true;
                        } catch (java.net.BindException ex) {
                            // ignored
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    System.err.println("S: (ready)");
                    Socket clientSocket = serverSocket.accept();
                    String request = escapeLineEnds(readRequest(clientSocket.getInputStream()));
                    System.err.println("S: request: " + request);
                    clientSocket.getOutputStream().write(response.getBytes());
                    System.err.println("S: response: " + escapeLineEnds(response));
                    clientSocket.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                } finally {
                    if (serverSocket != null) {
                        try {
                            serverSocket.close();
                            System.err.println("S: (closed)");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            };
        };
        Thread t = new Thread(server);
        t.start();
        return t;
    }

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

    private void testJDKHttpClient(Thread server) throws IOException, InterruptedException {
        try {
            HttpClient client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build();
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(TESTURI)).GET().build();
            HttpResponse<String> response = client.send(request, BodyHandlers.ofString());

            int status = response.statusCode();
            String body = response.body();

            System.err.println("C: status: " + status);
            System.err.println("C: body: " + escapeLineEnds(body));

            Assert.assertEquals(CONTENT, body);
            Assert.assertEquals(200, status);
        } finally {
            server.join();
        }
    }

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

    protected Thread create100Server() throws IOException {
        return createServer(100, "Continue", null);
    }

    protected Thread create102Server() throws IOException {
        return createServer(102, "Processing", "Status-URI: 404 <x>" + CRLF);
    }

    protected Thread create103Server() throws IOException {
        return createServer(103, "Early Hint", "Link: </p>; rel=prefetch" + CRLF);
    }

    protected Thread create199Server() throws IOException {
        return createServer(199, "", null);
    }

    protected Thread create200Server() throws IOException {
        return createServer(-1, null, null);
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

    @Test
    public void testJDKHttpClient100() throws IOException, InterruptedException {
        testJDKHttpClient(create100Server());
    }

    @Test
    public void testJDKHttpClient102() throws IOException, InterruptedException {
        testJDKHttpClient(create102Server());
    }

    @Test
    public void testJDKHttpClient103() throws IOException, InterruptedException {
        testJDKHttpClient(create103Server());
    }

    @Test
    public void testJDKHttpClient199() throws IOException, InterruptedException {
        testJDKHttpClient(create199Server());
    }

    @Test
    public void testJDKHttpClient200() throws IOException, InterruptedException {
        testJDKHttpClient(create200Server());
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

    public static String readFully(InputStream is) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[16384];

        while ((nRead = is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }

        buffer.flush();

        return new String(buffer.toByteArray());
    }

    public static String readRequest(InputStream is) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[16384];

        while ((nRead = is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
            if (new String(buffer.toByteArray()).indexOf(CRLF + CRLF) >= 0)
                break;
        }

        buffer.flush();

        return new String(buffer.toByteArray());
    }

    protected static String escapeLineEnds(String s) {
        return s.replace("\r", "<CR>").replace("\n", "<LF>");
    }
}
