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
- Same for OkHttp
- The Apache HttpClient libraries work as specified (the newer one can expose the 1xx information)

Bug Reports:

- java.net.HttpURLConnection: https://bugs.openjdk.org/browse/JDK-8170305 (2016-11) - see https://github.com/openjdk/jdk/pull/10229 - fixed in JDK 20
- java.net.http.HttpClient: https://bugs.java.com/bugdatabase/view_bug.do?bug_id=JDK-8293574 (2022-09) - see https://github.com/openjdk/jdk/pull/10169
- OkHttp: https://github.com/square/okhttp/issues/7440, found to be a dupe of https://github.com/square/okhttp/issues/2257

Fixes in JDKs:

| java.net.HttpURLConnection | java.net.http.HttpClient |
| ------------- | ------------- |
| JDK 8 :x:     | JDK 8 :x:   |
| JDK 11 :x:    | JDK 11 :x:   |
| JDK 17 :x:    | JDK 17 :x:   |
| JDK 19 :x: (but see https://github.com/openjdk/jdk19u/pull/27)   | JDK 19 :x:   |
| JDK 20 :heavy_check_mark: (build 20-ea+15-1019)   | JDK 20 :x:   |
