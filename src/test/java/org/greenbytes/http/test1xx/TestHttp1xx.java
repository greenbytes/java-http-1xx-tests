package org.greenbytes.http.test1xx;

import org.junit.Rule;
import org.junit.rules.TestName;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

public class TestHttp1xx {

    protected static String CONTENT = "Hello, world.";

    protected static final String ANSI_RESET = "\u001B[0m";
    protected static final String ANSI_BOLD = "\u001B[1m";
    protected static final String ANSI_FAINT = "\u001B[2m";
    protected static final String ANSI_ITALIC = "\u001B[3m";
    protected static final String ANSI_RED = "\u001B[31m";
    protected static final String ANSI_BLUE = "\u001B[34m";
    protected static final String ANSI_MAGENTA = "\u001B[35m";

    private static final String CRLF = String.format("%c%c", 13, 10);
    private static final String FINALMESSAGE = "HTTP/1.1 200 OK" + CRLF + "Content-Type: text/plain" + CRLF + "Content-Length: "
            + CONTENT.length() + CRLF + CRLF + CONTENT;
    private static final int PORT = 8080;

    protected static String TESTURI = "http://localhost:" + PORT;

    @Rule
    public TestName name = new TestName();

    private Thread createServer(int status, String reason, String fields) {
        return createServer(status, reason, fields,1);
    }

    private Thread createServer(int status, String reason, String fields, int times) {
        Runnable server = () -> {
            ServerSocket serverSocket = null;
            try {
                StringBuilder wireResponse = new StringBuilder();
                String statusLine = "HTTP/1.1 " + status + " " + reason + CRLF;
                if (status >= 0) {
                    for (int i = 0; i < times; i++) {
                        wireResponse.append(statusLine);
                        wireResponse.append(fields);
                        wireResponse.append(CRLF);
                    }
                }
                wireResponse.append(FINALMESSAGE);

                System.err.println();
                System.err.println(ANSI_ITALIC + "--- Testing: " + status + (times > 1 ? (" * " + times) : "") + " --- (" + name.getMethodName() + ")" + ANSI_RESET);

                long start = System.currentTimeMillis();
                while (serverSocket == null) {
                    try {
                        serverSocket = new ServerSocket(PORT);
                    } catch (java.net.BindException ex) {
                        // ignored
                        try {
                            if (System.currentTimeMillis() > start + TimeUnit.SECONDS.toMillis(10)) {
                                System.err.println("S: (giving up)");
                                throw new IllegalStateException("timeout trying to bind");
                            }
                            System.err.println("S: (trying to bind to port " + PORT + ")");
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }

                System.err.println("S: (ready)");
                serverSocket.setSoTimeout((int)TimeUnit.SECONDS.toMillis(2));
                Socket clientSocket = serverSocket.accept();

                try (InputStream is = clientSocket.getInputStream()) {
                    String request = ANSI_BLUE + escapeLineEnds(readRequest(is), ANSI_FAINT, ANSI_RESET + ANSI_BLUE) + ANSI_RESET;
                    System.err.println("S: request: " + request);
                    clientSocket.getOutputStream().write(wireResponse.toString().getBytes());
                    System.err.println("S: response: " + ANSI_MAGENTA + escapeLineEnds(wireResponse.toString(), ANSI_FAINT, ANSI_RESET + ANSI_MAGENTA) + ANSI_RESET);
                    clientSocket.close();
                }
            } catch (IOException ex) {
                System.err.println("S: exception: " + ANSI_RED + ex.getMessage() + ANSI_RESET);
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
        Thread t = new Thread(server);
        t.start();
        return t;
    }

    protected Thread create100Server() {
        return createServer(100, "Continue", "");
    }

    protected Thread create102Server() {
        return createServer(102, "Processing", "Status-URI: 404 <x>" + CRLF);
    }

    protected Thread create103Server() {
        return createServer(103, "Early Hint", "Link: </p>; rel=prefetch" + CRLF);
    }

    protected Thread create104Server(int times) {
        return createServer(104, "Upload Resumption Supported", "Upload-Offset: 50" + CRLF, times);
    }

    protected Thread create199Server() {
        return createServer(199, "", "");
    }

    protected Thread create200Server() {
        return createServer(-1, null, "");
    }

    protected static String readFully(InputStream is) throws IOException {
        return new String(is.readAllBytes(), StandardCharsets.UTF_8);
    }

    protected static String readRequest(InputStream is) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[16384];

        while ((nRead = is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
            if (buffer.toString().contains(CRLF + CRLF))
                break;
        }

        buffer.flush();

        return buffer.toString();
    }

    protected static String escapeLineEnds(String s) {
        return escapeLineEnds(s, "", "");
    }

    protected static String escapeLineEnds(String s, String before, String after) {
        return s.replace("\r", before + "<CR>" + after).replace("\n", before + "<LF>" + after);
    }

    protected static void logStatus(int status, String reasonPhrase) {
        System.err.println("C:  status: " + ANSI_BOLD + status + ANSI_RESET + " " + reasonPhrase);
    }

    protected static void logIStatus(int status, String reasonPhrase) {
        System.err.println("C: istatus: " + ANSI_BOLD + status + ANSI_RESET + " " + reasonPhrase);
    }

    protected static void logContent(String content) {
        System.err.println("C: content: " + escapeLineEnds(content));
    }
}
