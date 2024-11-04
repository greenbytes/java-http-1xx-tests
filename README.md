# java-http-1xx-tests
Tests for HTTP client support for informational (1xx) response messages, as defined in [Section 15.2 of RFC 9110](https://www.rfc-editor.org/rfc/rfc9110.html#name-informational-1xx).

Clients minimally should ignore these messages and wait for a final message (non-1xx). Optimally, they would expose receipt of an 1xx to the client, so that it can process it early, without waiting for the final response.

We currently test with:

- java.net.HttpURLConnection
- java.net.http.HttpClient (JDK 11)
- Apache HttpClient 4
- Apache HttpClient 5
- OkHttp

Results:

- Both JDK clients handle 1xx as final response, exposing the actual final response as response body (at least java.net.HttpURLConnection special-cases status code 100 correctly)
- The Apache HttpClient libraries work as specified (the newer one can expose the 1xx information)
- OkHttp fails to handle multiple informational messages

Bug Reports:

- java.net.HttpURLConnection: https://bugs.openjdk.org/browse/JDK-8170305 (2016-11) - see https://github.com/openjdk/jdk/pull/10229 - fixed in JDK 20
- java.net.http.HttpClient: https://bugs.java.com/bugdatabase/view_bug?bug_id=8293574 (2022-09) - see https://github.com/openjdk/jdk/pull/10169 - fixed in JDK 20
- OkHttp: https://github.com/square/okhttp/issues/7440 (fixed in latest 4.x version), found to be a dupe of https://github.com/square/okhttp/issues/2257
- OkHttp: multiple 1xx responses: https://github.com/square/okhttp/issues/8568

Enhancement requests:

- expose 1xx responses in java.net.http.HttpClient: https://bugs.openjdk.org/browse/JDK-8294196

Fixes in JDKs:

| java.net.HttpURLConnection | java.net.http.HttpClient |
| ------------- | ------------- |
| JDK 8 :x:     | JDK 8 :x:   |
| JDK 11 :x:    | JDK 11 :x:   |
| JDK 17 :x:    | JDK 17 :x:   |
| JDK 21 :heavy_check_mark: | :heavy_check_mark: |
