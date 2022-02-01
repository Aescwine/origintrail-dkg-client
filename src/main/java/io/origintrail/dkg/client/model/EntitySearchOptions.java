package io.origintrail.dkg.client.model;

import lombok.Builder;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Class for encapsulating DKG GET /entities:search request parameters.
 */
@Builder
@Getter
public class EntitySearchOptions {

    private final String query;
    private final String ids;
    private final String issuers;
    private final String types;
    private final Boolean prefix;
    private final String framingCriteria;
    private final Integer limit;
    private final Boolean load;

    public Map<String, String> getQueryParameters() {
        Map<String, String> queryParams = new HashMap<>();

        if (StringUtils.isNoneBlank(query)) {
            queryParams.put("query", query);
        }
        if (StringUtils.isNoneBlank(ids)) {
            queryParams.put("ids", ids);
        }
        if (StringUtils.isNoneBlank(issuers)) {
            queryParams.put("issuers", issuers);
        }
        if (StringUtils.isNoneBlank(types)) {
            queryParams.put("types", types);
        }
        if (prefix != null) {
            queryParams.put("prefix", prefix.toString());
        }
        if (StringUtils.isNoneBlank(framingCriteria)) {
            queryParams.put("framingCriteria", framingCriteria);
        }
        if (limit != null) {
            queryParams.put("limit", limit.toString());
        }
        if (load != null) {
            queryParams.put("load", load.toString());
        }

        return queryParams;
    }
}
