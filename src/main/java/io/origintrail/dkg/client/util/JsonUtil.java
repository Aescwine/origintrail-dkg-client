package io.origintrail.dkg.client.util;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class JsonUtil {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private JsonUtil() {
    }

    public static boolean isJsonValid(String jsonInString) {
        try {
            MAPPER.readTree(jsonInString);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static boolean isJsonValid(byte[] jsonInByteArray) {
        try {
            MAPPER.readTree(jsonInByteArray);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
