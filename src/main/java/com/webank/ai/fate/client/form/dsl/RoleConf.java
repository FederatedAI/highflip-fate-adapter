package com.webank.ai.fate.client.form.dsl;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.webank.ai.fate.common.deserializer.JsonMapMapDeserializer;
import lombok.Data;

import java.util.Map;

@Data
public class RoleConf {

    @JsonAnyGetter
    @JsonDeserialize(using = JsonMapMapDeserializer.class)
    private Map<String, Map<String, String>> host;

    @JsonAnyGetter
    @JsonDeserialize(using = JsonMapMapDeserializer.class)
    private Map<String, Map<String, String>> guest;

}
