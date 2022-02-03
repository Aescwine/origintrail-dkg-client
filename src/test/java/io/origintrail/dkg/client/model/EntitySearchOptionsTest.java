package io.origintrail.dkg.client.model;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

class EntitySearchOptionsTest {

    @Test
    void getQueryParameters_containsAllOptions_returnsMapWithAllQueryParameters() {
        // given
        EntitySearchOptions entitySearchOptions = EntitySearchOptions
                .builder()
                .query("keyword")
                .ids("12345")
                .issuers("issuerName")
                .types("entityType")
                .prefix(true)
                .framingCriteria("criteria")
                .limit(10)
                .load(true)
                .build();

        // when
        Map<String, String> queryParameters = entitySearchOptions.getQueryParameters();

        // then
        assertThat(queryParameters).containsOnly(
                entry("query", "keyword"),
                entry("ids", "12345"),
                entry("issuers", "issuerName"),
                entry("types", "entityType"),
                entry("prefix", "true"),
                entry("framingCriteria", "criteria"),
                entry("limit", "10"),
                entry("load", "true")
        );
    }

    @Test
    void getQueryParameters_containsNoOptions_returnsEmptyMap() {
        // given
        EntitySearchOptions entitySearchOptions = EntitySearchOptions.builder().build();

        // when
        Map<String, String> queryParameters = entitySearchOptions.getQueryParameters();

        // then
        assertThat(queryParameters).isEmpty();
    }
}