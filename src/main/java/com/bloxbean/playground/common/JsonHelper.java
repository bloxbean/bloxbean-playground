package com.bloxbean.playground.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Singleton
@Slf4j
public class JsonHelper {

    @Inject
    private ObjectMapper mapper;

    public String getJson(Object obj) {
        if (obj == null) {
            return null;
        } else {
            try {
                return mapper.writeValueAsString(obj);
            } catch (JsonProcessingException e) {
                log.error("Error converting obj to json");
                return obj.toString();
            }
        }
    }

    public String getPrettyJson(Object obj) {
        if (obj == null) {
            return null;
        } else {
            try {
                return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
            } catch (JsonProcessingException e) {
                log.error("Error converting obj to json");
                return obj.toString();
            }
        }
    }

    public String getPrettyJson(String jsonStr) {
        if (jsonStr == null) {
            return null;
        } else {
            try {
                Object json = mapper.readValue(jsonStr, Object.class);
                return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
            } catch (Exception var2) {
                return jsonStr;
            }
        }
    }

    public <T> T toObject(String content, Class<T> clazz) throws JsonProcessingException {
        return mapper.readValue(content, clazz);
    }

    public <T> List<T> toList(String content, Class<T> clazz) throws JsonProcessingException {
        return mapper.readValue(content, new TypeReference<List<T>>() {
        });
    }

    public Object toList(String content, TypeReference typeReference) throws JsonProcessingException {
        return mapper.readValue(content, typeReference);
    }
}
