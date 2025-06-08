# java-http-1xx-tests
Tests for HTTP client support for informational (1xx) response messages, as defined in [Section 15.2 of RFC 9110](https://www.rfc-editor.org/rfc/rfc9110.html#name-informational-1xx).

Clients minimally should ignore these messages and wait for a final message (non-1xx). Optimally, they would expose receipt of an 1xx to the client, so that it can process it early, without waiting for the final response.

We currently test with:

- java.net.HttpURLConnection
- java.net.http.HttpClient (JDK 11)
- Apache HttpClient 4
- Apache HttpClient 5
- OkHttp

Test overview:

| Client/Test                               | 100 | 102 | 103 | 104 | 104 x 2            | 104 x 100                                                                                                                                           | 199                 | 200 |
|-------------------------------------------|-----|-----|---- |-----|--------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------|---------------------| --- |
| java.net.HttpURLConnection (Java 8 .. 17) | :heavy_check_mark: | :x: | :x: | :x: | :x:                | :x:                                                                                                                                                 | :x:                 | :heavy_check_mark: |
| java.net.HttpURLConnection (Java 21)      | :heavy_check_mark: | :heavy_check_mark: | :heavy_check_mark: | :heavy_check_mark: | :heavy_check_mark: | :heavy_check_mark:                                                                                                                                  | :heavy_check_mark:  | :heavy_check_mark: |
| java.net.http.HttpClient                  | :heavy_check_mark: | :heavy_check_mark: | :heavy_check_mark: | :heavy_check_mark: | :heavy_check_mark: | :heavy_check_mark: (requires system property to be set to override the default limit to eight 1xx responses) | :heavy_check_mark:  | :heavy_check_mark: |
| Apache HttpClient 4                       | :heavy_check_mark: | :heavy_check_mark: | :heavy_check_mark: | :heavy_check_mark: | :heavy_check_mark: | :heavy_check_mark:                                                                                                                                  | :heavy_check_mark:  | :heavy_check_mark: |
| Apache HttpClient 5                       | :heavy_check_mark: | :heavy_check_mark: | :heavy_check_mark: | :heavy_check_mark: | :heavy_check_mark: | :heavy_check_mark:                                                                                                                                  | :heavy_check_mark:  | :heavy_check_mark: |
| OkHttp 4                                  | :heavy_check_mark: | :heavy_check_mark: | :heavy_check_mark: | :heavy_check_mark: | :x:                | :x:                                                                                                                                                 | :heavy_check_mark:  | :heavy_check_mark: |

Results:

- java.net.http.HttpClient works (in JDK 11, 17, and 21), but is limited to 8 1xx responses - that default can be overridden with https://docs.oracle.com/en/java/javase/24/docs/api/java.net.http/module-summary.html#jdk.httpclient.maxNonFinalResponses
- java.net.HttpURLConnection works (as of JDK 21), earlier versions fail (though special-cases status code 100 correctly)
- The Apache HttpClient libraries work as specified (HttpClient 5 even exposes the 1xx responses to the client)
- OkHttp fails to handle multiple informational messages


Bug Reports:

- java.net.HttpURLConnection: https://bugs.openjdk.org/browse/JDK-8170305 (2016-11) - see https://github.com/openjdk/jdk/pull/10229 - fixed in JDK 20
- java.net.http.HttpClient: https://bugs.java.com/bugdatabase/view_bug?bug_id=8293574 (2022-09) - see https://github.com/openjdk/jdk/pull/10169 - fixed in JDK 20, backported to JDKs 11 and 17
- OkHttp: https://github.com/square/okhttp/issues/7440 (fixed in latest 4.x version), found to be a dupe of https://github.com/square/okhttp/issues/2257
- OkHttp: multiple 1xx responses: https://github.com/square/okhttp/issues/8568

Enhancement requests:

- expose 1xx responses in java.net.http.HttpClient: https://bugs.openjdk.org/browse/JDK-8294196

Fixes in JDKs:

| java.net.HttpURLConnection | java.net.http.HttpClient |
| ------------- | ------------- |
| JDK 8 :x:     | JDK 8 :x:   |
| JDK 11 :x:    | JDK 11 :heavy_check_mark: (as of 11.0.24)  |
| JDK 17 :x:    | JDK 17 :heavy_check_mark: (as of 17.0.12) |
| JDK 21 :heavy_check_mark: | :heavy_check_mark: |

## Summary of currently defined 1xx codes

Note that although only few code points have been allocated (https://www.iana.org/assignments/http-status-codes/http-status-codes.xhtml),
the default behavior for all codes except 100 and 101 is the same, so HTTP stacks can and should treat them uniformly.

### 100 Continue

https://www.rfc-editor.org/rfc/rfc9110.html#status.100

Special: in conjunction with https://www.rfc-editor.org/rfc/rfc9110.html#field.expect can be used to check that a request will be accepted by the server before sending the body.

### 101 Switching Protocols

https://www.rfc-editor.org/rfc/rfc9110.html#status.101

Special: for changing protocols; needs to be hard-wired into clients.

### 102 Processing

http://webdav.org/specs/rfc2518.html#STATUS_102

Designed for reporting progress for a long-running request; could be used by user-agent to keep the connection open and display progress.

### 103 Early Hints

https://www.rfc-editor.org/rfc/rfc8297.html

These reponses will contain "early hints", allowing a user-agent to start fetching related documents (for instance CSS) before the initial request is completed.


### 104 Upload Resumption Supported

https://www.ietf.org/archive/id/draft-ietf-httpbis-resumable-upload-09.html

Can be used to (a) indicate that resumable uploads are supported, and (b) to report progress. Note that specifically for this code, the intermediate response can appear many times.

