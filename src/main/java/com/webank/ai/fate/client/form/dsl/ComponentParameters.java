package com.webank.ai.fate.client.form.dsl;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.webank.ai.fate.common.deserializer.JsonMapStringDeserializer;
import lombok.Data;

import java.util.Map;

@Data
public class ComponentParameters {

    @JsonDeserialize(using = JsonMapStringDeserializer.class)
    private Map<String, Map<String, String>> common;

    private RoleConf role;

}
