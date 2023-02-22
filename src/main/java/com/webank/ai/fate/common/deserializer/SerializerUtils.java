package com.webank.ai.fate.common.deserializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.ai.fate.client.form.dsl.Component;

import java.util.HashMap;
import java.util.Map;

public class SerializerUtils {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private SerializerUtils() {
    }

    public static <T> Map<String, T> mapObjectDeserialize(String object, Class<T> tClass)
            throws JsonProcessingException {
        if (object == null || object.isBlank()) {
            return null;
        }
        Map<String, T> ret = new HashMap<>();
        Map<String, Object> map = objectMapper.readValue(object, new TypeReference<Map<String, Object>>() {
        });
        for (String tempKey : map.keySet()) {
            Object tempValue = map.get(tempKey);
            if (tempValue != null) {
                String decodeValue = objectMapper.writeValueAsString(tempValue);
                if (tClass.isAssignableFrom(String.class)) {
                    //noinspection unchecked
                    ret.put(tempKey, (T) decodeValue);
                } else {
                    T result = objectMapper.readValue(decodeValue, tClass);
                    ret.put(tempKey, result);
                }
            }
        }
        return ret;
    }

    public static String toJsonString(Object object) throws JsonProcessingException {
        return objectMapper.writeValueAsString(object);
    }

    public static <T> T deserialize(String json, Class<T> tClass) throws JsonProcessingException {
        return objectMapper.readValue(json, tClass);
    }

    public static void main(String[] args) {
        String data = "{\n" + "        \"reader_0\": {\n" + "            \"module\": \"Reader\",\n"
                + "            \"output\": {\n" + "                \"data\": [\n" + "                    \"data\"\n"
                + "                ]\n" + "            }\n" + "        },\n" + "        \"reader_1\": {\n"
                + "            \"module\": \"Reader\",\n" + "            \"output\": {\n"
                + "                \"data\": [\n" + "                    \"data\"\n" + "                ]\n"
                + "            }\n" + "        },\n" + "        \"data_transform_0\": {\n"
                + "            \"module\": \"DataTransform\",\n" + "            \"input\": {\n"
                + "                \"data\": {\n" + "                    \"data\": [\n"
                + "                        \"reader_0.data\"\n" + "                    ]\n" + "                }\n"
                + "            },\n" + "            \"output\": {\n" + "                \"data\": [\n"
                + "                    \"data\"\n" + "                ],\n" + "                \"model\": [\n"
                + "                    \"model\"\n" + "                ]\n" + "            }\n" + "        },\n"
                + "        \"data_transform_1\": {\n" + "            \"module\": \"DataTransform\",\n"
                + "            \"input\": {\n" + "                \"data\": {\n" + "                    \"data\": [\n"
                + "                        \"reader_1.data\"\n" + "                    ]\n" + "                }\n"
                + "            },\n" + "            \"output\": {\n" + "                \"data\": [\n"
                + "                    \"data\"\n" + "                ],\n" + "                \"model\": [\n"
                + "                    \"model\"\n" + "                ]\n" + "            }\n" + "        },\n"
                + "        \"intersection_0\": {\n" + "            \"module\": \"Intersection\",\n"
                + "            \"input\": {\n" + "                \"data\": {\n" + "                    \"data\": [\n"
                + "                        \"data_transform_0.data\"\n" + "                    ]\n"
                + "                }\n" + "            },\n" + "            \"output\": {\n"
                + "                \"data\": [\n" + "                    \"data\"\n" + "                ],\n"
                + "                \"cache\": [\n" + "                    \"cache\"\n" + "                ]\n"
                + "            }\n" + "        },\n" + "        \"intersection_1\": {\n"
                + "            \"module\": \"Intersection\",\n" + "            \"input\": {\n"
                + "                \"data\": {\n" + "                    \"data\": [\n"
                + "                        \"data_transform_1.data\"\n" + "                    ]\n"
                + "                }\n" + "            },\n" + "            \"output\": {\n"
                + "                \"data\": [\n" + "                    \"data\"\n" + "                ],\n"
                + "                \"cache\": [\n" + "                    \"cache\"\n" + "                ]\n"
                + "            }\n" + "        },\n" + "        \"hetero_nn_0\": {\n"
                + "            \"module\": \"HeteroNN\",\n" + "            \"input\": {\n"
                + "                \"data\": {\n" + "                    \"train_data\": [\n"
                + "                        \"intersection_0.data\"\n" + "                    ],\n"
                + "                    \"validate_data\": [\n" + "                        \"intersection_1.data\"\n"
                + "                    ]\n" + "                }\n" + "            },\n" + "            \"output\": {\n"
                + "                \"data\": [\n" + "                    \"data\"\n" + "                ],\n"
                + "                \"model\": [\n" + "                    \"model\"\n" + "                ]\n"
                + "            }\n" + "        },\n" + "        \"hetero_nn_1\": {\n"
                + "            \"module\": \"HeteroNN\",\n" + "            \"input\": {\n"
                + "                \"data\": {\n" + "                    \"test_data\": [\n"
                + "                        \"intersection_1.data\"\n" + "                    ]\n"
                + "                },\n" + "                \"model\": [\n"
                + "                    \"hetero_nn_0.model\"\n" + "                ]\n" + "            },\n"
                + "            \"output\": {\n" + "                \"data\": [\n" + "                    \"data\"\n"
                + "                ],\n" + "                \"model\": [\n" + "                    \"model\"\n"
                + "                ]\n" + "            }\n" + "        },\n" + "        \"evaluation_0\": {\n"
                + "            \"module\": \"Evaluation\",\n" + "            \"input\": {\n"
                + "                \"data\": {\n" + "                    \"data\": [\n"
                + "                        \"hetero_nn_0.data\",\n" + "                        \"hetero_nn_1.data\"\n"
                + "                    ]\n" + "                }\n" + "            },\n" + "            \"output\": {\n"
                + "                \"data\": [\n" + "                    \"data\"\n" + "                ]\n"
                + "            }\n" + "        }\n" + "}";
        try {
            Map<String, Component> result = SerializerUtils.mapObjectDeserialize(data, Component.class);
            System.out.println(result.entrySet());
            System.out.println(SerializerUtils.toJsonString(result));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
