package com.webank.ai.fate.translator;

import com.baidu.highflip.core.entity.dag.Graph;
import com.baidu.highflip.core.entity.dag.Node;
import com.baidu.highflip.core.entity.dag.Party;
import com.baidu.highflip.core.entity.dag.PartyNode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.webank.ai.fate.client.form.dsl.Component;
import com.webank.ai.fate.client.form.dsl.ComponentParameters;
import com.webank.ai.fate.client.form.dsl.Dsl;
import com.webank.ai.fate.client.form.dsl.DslConf;
import com.webank.ai.fate.client.form.dsl.Input;
import com.webank.ai.fate.client.form.dsl.Output;
import com.webank.ai.fate.client.form.dsl.RoleConf;
import com.webank.ai.fate.common.deserializer.JsonListDeserializer;
import com.webank.ai.fate.common.deserializer.SerializerUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Data
public class DSLTranslator {

    public FateDAG translate(Graph dag) {
        Map<String, Component> componentMap = new HashMap<>();
        Map<String, Map<String, Object>> componentConfMap = new HashMap<>();
        Map<String, List<String>> role = new HashMap<>();
        Map<String, Map<String, Object>> hostConf = new HashMap<>();
        Map<String, Map<String, Object>> guestConf = new HashMap<>();
        Map<String, Map<String, Object>> arbiterConf = new HashMap<>();
        for (Node node : dag.getNodes().values()) {
            Component component = new Component();
            component.setInput(transInput(node.getInputs()));
            component.setOutput(transOutput(node.getOutputs()));
            component.setModule(node.getType());
            Map<String, Object> conf = new HashMap<>();
            for (String key : node.getAttributes().keySet()) {
                conf.put(key, node.getAttribute(key, null));
            }
            componentConfMap.put(node.getName(), conf);
            componentMap.put(node.getName(), component);
        }
        int guestIndex = 0;
        int hostIndex = 0;
        int arbiterIndex = 0;
        Map<String, List<String>> roleMap = new HashMap<>();
        List<String> guest = new ArrayList<>();
        List<String> host = new ArrayList<>();
        List<String> arbiter = new ArrayList<>();
        for (Party party : dag.getParties().values()) {
            role.putIfAbsent(party.getRole(), new ArrayList<>());
            role.get(party.getRole()).add(party.getName());
            Map<String, Object> componentAttr = new HashMap<>();
            for (PartyNode node : party.getNodes().values()) {
                Map<String, Object> attr = node.getAttributes();
                componentAttr.put(node.getName(), attr);
            }
            if (party.getRole().equalsIgnoreCase("host")) {
                hostConf.put(hostIndex + "", componentAttr);
                host.add(party.getName());
                hostIndex++;
            }
            if (party.getRole().equalsIgnoreCase("guest")) {
                guestConf.put(guestIndex + "", componentAttr);
                guest.add(party.getName());
                guestIndex++;
            }
            if (party.getRole().equalsIgnoreCase("arbiter")) {
                arbiterConf.put(arbiterIndex + "", componentAttr);
                arbiter.add(party.getName());
                arbiterIndex++;
            }
        }
        if (!guest.isEmpty()) {
            roleMap.put("guest", guest);
        }
        if (!host.isEmpty()) {
            roleMap.put("host", host);
        }
        if (!arbiter.isEmpty()) {
            roleMap.put("arbiter", arbiter);
        }
        Dsl dsl = new Dsl();
        dsl.setComponents(componentMap);
        DslConf dslConf = new DslConf();
        dslConf.setDsl_version("2");
        dslConf.setRole(roleMap);
        ComponentParameters componentParameters = new ComponentParameters();
        componentParameters.setCommon(componentConfMap);
        RoleConf roleConf = new RoleConf();
        if (!hostConf.isEmpty()) {
            roleConf.setHost(hostConf);
        }
        if (!guestConf.isEmpty()) {
            roleConf.setGuest(guestConf);
        }
        if (!arbiterConf.isEmpty()) {
            roleConf.setArbiter(arbiterConf);
        }
        componentParameters.setRole(roleConf);
        dslConf.setComponent_parameters(componentParameters);
        FateDAG fateDAG = new FateDAG();
        fateDAG.setDsl(dsl);
        fateDAG.setConf(dslConf);
        return fateDAG;
    }

    public Graph translate(FateDAG dag) {
        Graph graph = new Graph();
        Dsl dsl = dag.getDsl();
        Node pre = null;
        Map<String, Node> nodeMap = new HashMap<>();
        Map<String, Party> partyMap = new HashMap<>();
        Map<String, List<Party>> rolePartyMap = new HashMap<>();
        graph.setNodes(nodeMap);
        graph.setParties(partyMap);
        for (Map.Entry<String, Component> entity : dsl.getComponents().entrySet()) {
            String key = entity.getKey();
            Component component = entity.getValue();
            Node node = new Node();
            node.setName(key);
            node.setType(component.getModule());
            if (component.getInput() != null) {
                node.setInputs(transInput(component.getInput()));
            }
            if (component.getOutput() != null) {
                node.setOutputs(transOutput(component.getOutput()));
            }
            if (pre != null) {
                node.setParent(pre);
            }
            pre = node;
            nodeMap.put(key, node);
        }
        DslConf dslConf = dag.getConf();
        for (Map.Entry<String, List<String>> roleEntity : dslConf.getRole().entrySet()) {
            String role = roleEntity.getKey();
            rolePartyMap.putIfAbsent(role.toLowerCase(), new ArrayList<>());
            for (String partyId : roleEntity.getValue()) {
                Party party = new Party();
                party.setName(partyId);
                party.setRole(role);
                party.setNodes(new HashMap<>());
                partyMap.put(role + "-" + partyId, party);
                rolePartyMap.get(role.toLowerCase()).add(party);
            }
        }
        ComponentParameters componentParameters = dslConf.getComponent_parameters();
        for (Map.Entry<String, Map<String, Object>> attrMap : componentParameters.getCommon().entrySet()) {
            String componentName = attrMap.getKey();
            Map<String, Object> attr = attrMap.getValue();
            Node node = nodeMap.get(componentName);
            if (node == null) {
                throw new RuntimeException("dsl conf component not match");
            }
            attr.forEach(node::setAttribute);
        }
        if (componentParameters.getRole().getGuest() != null) {
            for (Map.Entry<String, Map<String, Object>> attr : componentParameters.getRole().getGuest().entrySet()) {
                String index = attr.getKey();
                Map<String, Object> componentsAttrs = attr.getValue();
                Party party = rolePartyMap.get("guest").get(Integer.parseInt(index));
                addPartyNodes(componentsAttrs, party);
            }
        }
        if (componentParameters.getRole().getHost() != null) {
            for (Map.Entry<String, Map<String, Object>> attr : componentParameters.getRole().getHost().entrySet()) {
                String index = attr.getKey();
                Map<String, Object> componentsAttrs = attr.getValue();
                Party party = rolePartyMap.get("host").get(Integer.parseInt(index));
                addPartyNodes(componentsAttrs, party);
            }
        }
        if (componentParameters.getRole().getArbiter() != null) {
            for (Map.Entry<String, Map<String, Object>> attr : componentParameters.getRole().getArbiter().entrySet()) {
                String index = attr.getKey();
                Map<String, Object> componentsAttrs = attr.getValue();
                Party party = rolePartyMap.get("arbiter").get(Integer.parseInt(index));
                addPartyNodes(componentsAttrs, party);
            }
        }
        return graph;
    }

    private void addPartyNodes(Map<String, Object> componentsAttrs, Party party) {
        if (party == null) {
            throw new RuntimeException("dsl conf role not match");
        }
        for (Map.Entry<String, Object> componentEntry : componentsAttrs.entrySet()) {
            String componentName = componentEntry.getKey();
            Object componentAttr = componentEntry.getValue();
            PartyNode partyNode = new PartyNode();
            partyNode.setParent(party);
            partyNode.setName(componentName);
            Map<String, Object> attrMap;
            try {
                attrMap = SerializerUtils.deserializeType(SerializerUtils.toJsonString(componentAttr),
                        new TypeReference<Map<String, Object>>() {
                        });
                if (attrMap != null) {
                    partyNode.setAttributes(attrMap);
                }
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            party.getNodes().put(componentName, partyNode);
        }
    }

    private Map<String, String> transInput(Input input) {
        try {
            Map<String, String> map = new HashMap<>();
            if (input.getData() != null) {
                map.put("data", SerializerUtils.toJsonString(input.getData()));
            }
            if (input.getModel() != null) {
                map.put("model", SerializerUtils.toJsonString(input.getModel()));
            }
            if (input.getCache() != null) {
                map.put("cache", SerializerUtils.toJsonString(input.getCache()));
            }
            return map;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private Input transInput(Map<String, String> map) {
        try {
            if (map == null) {
                return null;
            }
            Input input = new Input();
            TypeReference<Map<String, List<String>>> typeReference = new TypeReference<>() {
            };
            String content = map.get("data");
            Map<String, List<String>> data = SerializerUtils.deserializeType(content, typeReference);
            TypeReference<List<String>> typeReference2 = new TypeReference<>() {
            };
            List<String> model = SerializerUtils.deserializeType(map.get("model"), typeReference2);
            List<String> cache = SerializerUtils.deserializeType(map.get("cache"), typeReference2);
            input.setData(data);
            input.setModel(model);
            input.setCache(cache);
            return input;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private Map<String, String> transOutput(Output output) {
        try {
            Map<String, String> map = new HashMap<>();
            map.put("data", SerializerUtils.toJsonString(output.getData()));
            map.put("model", SerializerUtils.toJsonString(output.getModel()));
            map.put("cache", SerializerUtils.toJsonString(output.getCache()));
            return map;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private Output transOutput(Map<String, String> map) {
        try {
            if (map == null) {
                return null;
            }
            TypeReference<List<String>> typeReference2 = new TypeReference<>() {
            };
            List<String> data = SerializerUtils.deserializeType(map.get("data"), typeReference2);
            List<String> model = SerializerUtils.deserializeType(map.get("model"), typeReference2);
            List<String> cache = SerializerUtils.deserializeType(map.get("cache"), typeReference2);
            Output output = new Output();
            output.setData(data);
            output.setModel(model);
            output.setCache(cache);
            return output;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FateDAG {

        Dsl dsl;

        DslConf conf;

    }
}
