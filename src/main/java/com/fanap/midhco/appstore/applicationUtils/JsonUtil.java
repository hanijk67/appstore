package com.fanap.midhco.appstore.applicationUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.deser.std.FromStringDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.json.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;

/**
 * Created by h.mehrara on 1/28/2015.
 */
public class JsonUtil {
    static final ObjectMapper mapper = new ObjectMapper();

    static {
        SimpleModule module = new SimpleModule();
        module.addSerializer(BigDecimal.class, new ToStringSerializer());
        module.addDeserializer(BigDecimal.class, new FromStringDeserializer<BigDecimal>(BigDecimal.class) {
            @Override
            protected BigDecimal _deserialize(String s, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
                return new BigDecimal(s);
            }
        });
        mapper.registerModule(module);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);


    }

    public static String getJson(Object obj) {
        try {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            mapper.writeValue(bout, obj);
            byte[] objectBytes = bout.toByteArray();
            return new String(objectBytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T getObject(byte[] json, Class<T> classOfT) {
        try {
            return mapper.readValue(new String(json, "utf-8"), classOfT);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T getObject(String json, Class<T> classOfT) {
        try {
            return mapper.readValue(json, classOfT);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T getObject(String json, TypeReference<T> typeReference) {
        try {
            return mapper.readValue(json, typeReference);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static JSONObject getJsonObject(String json) {
        try {
            return new JSONObject(json);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static JSONArray getJsonArray(String json) {
        try {
            return new JSONArray(json);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


}
