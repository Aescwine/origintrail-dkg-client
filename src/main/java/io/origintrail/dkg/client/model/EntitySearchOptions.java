package io.origintrail.dkg.client.model;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Class for encapsulating DKG GET /entities:search request parameters.
 */
@Builder
@Getter
@ToString
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

        if (StringUtils.isNotBlank(query)) {
            queryParams.put("query", query);
        }
        if (StringUtils.isNotBlank(ids)) {
            queryParams.put("ids", ids);
        }
        if (StringUtils.isNotBlank(issuers)) {
            queryParams.put("issuers", issuers);
        }
        if (StringUtils.isNotBlank(types)) {
            queryParams.put("types", types);
        }
        if (prefix != null) {
            queryParams.put("prefix", prefix.toString());
        }
        if (StringUtils.isNotBlank(framingCriteria)) {
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
