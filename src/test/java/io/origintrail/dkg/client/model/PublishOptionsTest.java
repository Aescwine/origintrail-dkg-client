package io.origintrail.dkg.client.model;

import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class PublishOptionsTest {

    @Test
    void builder_buildPublishOptions_createsObjectWithCorrectFieldsSet() {
        // given
        PublishOptions expectedPublishOptions = new PublishOptions(Collections.singletonList("aKeyword"), Visibility.PRIVATE, "8a6017ec52f7e6fb6b6671bd8ba2cf833e5a8e98454a3329239bb78c1f82465c");
        PublishOptions.PublishOptionsBuilder builder = PublishOptions.builder( Collections.singletonList("aKeyword")).visibility(Visibility.PRIVATE);

        // when
        PublishOptions publishOptions = builder.build();

        // then
        assertThat(publishOptions.getKeywords()).isEqualTo(expectedPublishOptions.getKeywords());
        assertThat(publishOptions.getVisibility()).isEqualTo(expectedPublishOptions.getVisibility());
    }
}