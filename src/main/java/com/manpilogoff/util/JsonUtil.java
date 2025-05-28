package com.manpilogoff.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

public final class JsonUtil {
    private static final ObjectWriter writer;

    static {
        ObjectMapper mapper = new ObjectMapper();
        writer = mapper.writerWithDefaultPrettyPrinter();
    }

    private JsonUtil() {
    }

    public static String toJson(Object data) {
        try {
            return writer.writeValueAsString(data);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize object to JSON", e);
        }
    }
}
