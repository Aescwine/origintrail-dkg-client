package io.origintrail.dkg.client.util;

import com.apicatalog.jsonld.StringUtils;
import io.origintrail.dkg.client.exception.UriCreationException;
import io.origintrail.dkg.client.model.HttpUrlOptions;
import org.apache.http.client.utils.URIBuilder;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Builder class for generating HTTP request URIs.
 */
public class UriUtil {

    private UriUtil() {
    }

    public static UriUtil.UriBuilder builder() {
        return new UriUtil.UriBuilder();
    }

    public static class UriBuilder {

        URIBuilder uriBuilder = new URIBuilder();

        public URI build() throws UriCreationException {
            try {
                return uriBuilder.build().toURL().toURI();
            } catch (Exception e) {
                throw new UriCreationException("Exception occurred creating Uri.", e);
            }
        }

        public UriBuilder httpUrlOptions(HttpUrlOptions options) {
            uriBuilder.setScheme(options.getScheme())
                    .setHost(options.getHost())
                    .setPort(options.getPort());
            return this;
        }

        /**
         * Set the path of the URI.
         *
         * @param path the URI path
         * @return This {@code UriBuilder} to be used in further construction of the {@link URI}.
         */
        public UriBuilder path(String path) {
            uriBuilder.setPath(path);
            return this;
        }

        /**
         * Add path segments to the URI.
         *
         * @param pathSegments {@code List<String>} of path segments to include in URI.
         * @return This {@code UriBuilder} to be used in further construction of the {@link URI}.
         */
        public UriBuilder pathSegments(List<String> pathSegments) {
            List<String> splitSegments = pathSegments.stream()
                    .map(s -> s.split("/"))
                    .flatMap(Arrays::stream).collect(Collectors.toList());

            uriBuilder.setPathSegments(splitSegments);
            return this;
        }

        /**
         * Add query parameters to the URI.
         *
         * @param parameters a {@code Map<String, String>} of key/value parameters, to include in the URI.
         * @return This {@code UriBuilder} to be used in further construction of the {@link URI}.
         */
        public UriBuilder queryParameters(Map<String, String> parameters) {
            if (parameters != null) {
                parameters.forEach((param, value) -> {
                    if (StringUtils.isNotBlank(value)) {
                        uriBuilder.addParameter(param, value);
                    }
                });
            }
            return this;
        }

        /**
         *  Add query parameter values for a single key to the URI. Represents an array of values for the given parameter key.
         *
         * @param key Query parameter key.
         * @param values A {@code List<String>} of values to be added to query parameters for the given {@code key}.
         * @return This {@code UriBuilder} to be used in further construction of the {@link URI}.
         */
        public UriBuilder queryParameters(String key, List<String> values) {
            if (StringUtils.isNotBlank(key) && !values.isEmpty()) {
                values.forEach(value -> {
                    if (StringUtils.isNotBlank(value)) {
                        uriBuilder.addParameter(key, value);
                    }
                });
            }
            return this;
        }
    }
}
