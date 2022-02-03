package io.origintrail.dkg.client.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PublishOptionsTest {

    @Test
    void builder_buildPublishOptions_createsObjectWithCorrectFieldsSet() {
        // given
        PublishOptions expectedPublishOptions = new PublishOptions("assetName", "aKeyword", true);
        PublishOptions.PublishOptionsBuilder builder = PublishOptions.builder("assetName").keywords("aKeyword").visibility(true);

        // when
        PublishOptions publishOptions = builder.build();

        // then
        assertThat(publishOptions.getAssets()).isEqualTo(expectedPublishOptions.getAssets());
        assertThat(publishOptions.getKeywords()).isEqualTo(expectedPublishOptions.getKeywords());
        assertThat(publishOptions.isVisibility()).isEqualTo(expectedPublishOptions.isVisibility());
    }
}