package org.greenbytes.http.test1xx;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class TestHttp1xx {

    protected static String CONTENT = "Hello, world.";

    private static String CRLF = String.format("%c%c", 13, 10);
    private static String FINALMESSAGE = "HTTP/1.1 200 OK" + CRLF + "Content-Type: text/plain" + CRLF + "Content-Length: "
            + CONTENT.length() + CRLF + CRLF + CONTENT;
    private static int PORT = 8080;

    protected static String TESTURI = "http://localhost:" + PORT;
 
    private Thread createServer(int status, String reason, String fields) throws IOException {
        return createServer(status, reason, fields,1);
    }

    private Thread createServer(int status, String reason, String fields, int times) throws IOException {
        Runnable server = new Runnable() {
            @Override
            public void run() {
                ServerSocket serverSocket = null;
                try {
                    String response = "";
                    if (status >= 0) {
                        for (int i = 0; i < times; i++) {
                            response += "HTTP/1.1 " + status + " " + reason + CRLF;
                            if (fields != null) {
                                response += fields;
                            }
                            response += CRLF;
                        }
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
                    System.err.println("");
                    System.err.println("--- " + status + (times > 1 ? (" * " + times) : "") + " ---");
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

    protected Thread create100Server() throws IOException {
        return createServer(100, "Continue", null);
    }

    protected Thread create102Server() throws IOException {
        return createServer(102, "Processing", "Status-URI: 404 <x>" + CRLF);
    }

    protected Thread create103Server() throws IOException {
        return createServer(103, "Early Hint", "Link: </p>; rel=prefetch" + CRLF);
    }

    protected Thread create104Server(int times) throws IOException {
        return createServer(104, "Upload Resumption Supported", "Upload-Offset: 50" + CRLF, times);
    }

    protected Thread create199Server() throws IOException {
        return createServer(199, "", null);
    }

    protected Thread create200Server() throws IOException {
        return createServer(-1, null, null);
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
