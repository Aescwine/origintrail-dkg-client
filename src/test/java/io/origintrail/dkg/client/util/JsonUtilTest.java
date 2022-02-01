package io.origintrail.dkg.client.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JsonUtilTest {

    private static final String VALID_JSON_STRING = "{\n" +
            "\t\"validJsonTest\": true,\n" +
            "\t\"jsonArray\": [\n" +
            "\t  \"item1\", \n" +
            "\t  \"item2\"\n" +
            "\t],\n" +
            "\t\"jsonMap\": {\n" +
            "\t\t\"key\": \"test\",\n" +
            "\t\t\"value\": \"isValid\"\n" +
            "\t}\n" +
            "}";

    private static final String INVALID_JSON_STRING = "{\n" +
            "\t\"validJsonTest\": false,\n" +
            "\t\"jsonArray\": [\n" +
            "\t\t\"item1\",\n" +
            "\t\t\"item2\"\n" +
            "\t],\n" +
            "\t\"jsonMap\": \"key\": \"test\",\n" +
            "\t\"value\": \"isNotValid\"\n" +
            "\n" +
            "}";

    @Test
    void isJsonValid_withValidJsonString_returnsTrue() {
        // when
        boolean isValid = JsonUtil.isJsonValid(VALID_JSON_STRING);

        // then
        assertThat(isValid).isTrue();

    }

    @Test
    void isJsonValid_withInvalidJsonString_returnsFalse() {
        // when
        boolean isValid = JsonUtil.isJsonValid(INVALID_JSON_STRING);

        // then
        assertThat(isValid).isFalse();
    }

    @Test
    void isJsonValid_withValidJsonByteArray_returnsTrue() {
        // given
        byte[] validByteArray = VALID_JSON_STRING.getBytes();

        // when
        boolean isValid = JsonUtil.isJsonValid(validByteArray);

        // then
        assertThat(isValid).isTrue();
    }

    @Test
    void isJsonValid_withInvalidJsonByteArray_returnsFalse() {
        // given
        byte[] invalidByteArray = INVALID_JSON_STRING.getBytes();

        // when
        boolean isValid = JsonUtil.isJsonValid(invalidByteArray);

        // then
        assertThat(isValid).isFalse();
    }
}