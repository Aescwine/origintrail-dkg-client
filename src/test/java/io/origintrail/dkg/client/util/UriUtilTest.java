package io.origintrail.dkg.client.util;

import io.origintrail.dkg.client.exception.UriCreationException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

class UriUtilTest {
    @Test
    void build_withInvalidPath_throwsUriCreationException() {
        // given
        String invalidPath = "invalidPath";

        // when
        UriCreationException throwable = catchThrowableOfType(
                () ->  UriUtil.builder().path(invalidPath).build(), UriCreationException.class);

        // then
        assertThat(throwable.getMessage()).isEqualTo("Exception occurred creating Uri.");
    }
}