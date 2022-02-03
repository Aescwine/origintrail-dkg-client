package io.origintrail.dkg.client.model;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Class for encapsulating DKG GET /assertions:search request parameters.
 */
@Getter
public class AssertionSearchOptions {

    private final String query;
    private final Boolean load;

    public AssertionSearchOptions(String query, Boolean load) {
        this.query = query;
        this.load = load;
    }

    public static AssertionSearchOptions.AssertionSearchOptionsBuilder builder(String query) {
        return new AssertionSearchOptions.AssertionSearchOptionsBuilder(query);
    }

    public static class AssertionSearchOptionsBuilder {
        private final String query;
        private Boolean load;

        AssertionSearchOptionsBuilder(String query) {
            this.query = query;
        }

        public AssertionSearchOptions.AssertionSearchOptionsBuilder load(boolean load) {
            this.load = load;
            return this;
        }

        public AssertionSearchOptions build() {
            return new AssertionSearchOptions(query, load);
        }
    }

    public Map<String, String> getQueryParameters() {
        Map<String, String> queryParams = new HashMap<>();

        if (StringUtils.isNotBlank(query)) {
            queryParams.put("query", query);
        }
        if (load != null) {
            queryParams.put("load", load.toString());
        }

        return queryParams;
    }
}
