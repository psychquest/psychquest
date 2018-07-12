package io.psychquest.app.util;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

/**
 * A convenient way to set up HTTP-Headers that are application specific.
 */
public class HttpHeaders {

    private HttpHeaders() {
    }

    public static HeaderBuilder authorization(String jwt) {
        HeaderBuilder builder = new HeaderBuilder();
        return builder.authorization("Bearer " + jwt);
    }

    public static HeaderBuilder errorAlert(String errorKey) {
        HeaderBuilder builder = new HeaderBuilder();
        return builder.errorAlert(errorKey);
    }

    /**
     * Builder implementation to have a fluent API for HTTP-Headers ;-)
     */
    public static class HeaderBuilder {
        private org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();

        public HeaderBuilder authorization(String bearerToken) {
            this.headers.add(AUTHORIZATION, bearerToken);
            return this;
        }

        public HeaderBuilder errorAlert(String errorKey) {
            this.headers.add("X-psychquest-alert", errorKey);
            return this;
        }

        public org.springframework.http.HttpHeaders build() {
            return this.headers;
        }

    }

}
