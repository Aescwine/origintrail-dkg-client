package io.origintrail.dkg.client.model;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

class AssertionSearchOptionsTest {

    @Test
    void getQueryParameters_optionsHasQueryAndLoadParameters_returnsMapWithQueryAndLoadParameters() {
        // given
        AssertionSearchOptions assertionSearchOptions = AssertionSearchOptions.builder("keyword").load(false).build();

        // when
        Map<String, String> queryParameters = assertionSearchOptions.getQueryParameters();

        // then
        assertThat(queryParameters).containsOnly(entry("query", "keyword"),entry("load", "false"));
    }

    @Test
    void getQueryParameters_optionsHasQueryParameter_returnsMapWithQueryParameterOnly() {
        // given
        AssertionSearchOptions keyword = AssertionSearchOptions.builder("keyword").build();

        // when
        Map<String, String> queryParameters = keyword.getQueryParameters();

        // then
        assertThat(queryParameters).containsOnly(entry("query", "keyword"));
    }

    @Test
    void builder_buildPublishOptions_createsObjectWithCorrectFieldsSet() {
        // given
        AssertionSearchOptions expectedAssertionSearchOptions = new AssertionSearchOptions("keyword", true);
        AssertionSearchOptions.AssertionSearchOptionsBuilder builder = AssertionSearchOptions.builder("keyword").load(true);

        // when
        AssertionSearchOptions assertionSearchOptions = builder.build();

        // then
        assertThat(assertionSearchOptions.getQuery()).isEqualTo(expectedAssertionSearchOptions.getQuery());
        assertThat(assertionSearchOptions.getLoad()).isEqualTo(expectedAssertionSearchOptions.getLoad());
    }
}