package com.webank.ai.fate.translator;

import com.baidu.highflip.core.entity.dag.Graph;
import com.baidu.highflip.core.entity.dag.Node;
import com.baidu.highflip.core.entity.dag.Party;
import com.baidu.highflip.core.entity.dag.PartyNode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.webank.ai.fate.client.form.dsl.Component;
import com.webank.ai.fate.client.form.dsl.ComponentParameters;
import com.webank.ai.fate.client.form.dsl.Dsl;
import com.webank.ai.fate.client.form.dsl.DslConf;
import com.webank.ai.fate.client.form.dsl.Input;
import com.webank.ai.fate.client.form.dsl.Output;
import com.webank.ai.fate.client.form.dsl.RoleConf;
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
@NoArgsConstructor
public class DSLTranslator {

    public FateDAG translate(Graph dag) {
        try {
            Map<String, Component> componentMap = new HashMap<>();
            Map<String, Map<String, String>> componentConfMap = new HashMap<>();
            Map<String, List<String>> role = new HashMap<>();
            Map<String, Map<String, String>> hostConf = new HashMap<>();
            Map<String, Map<String, String>> guestConf = new HashMap<>();
            for (Node node : dag.getNodes()) {
                Component component = new Component();
                component.setInput(transInput(node.getInputs()));
                component.setOutput(transOutput(node.getOutputs()));
                component.setModule(node.getType());
                Map<String, String> conf = new HashMap<>();
                for (String key : node.getAttributeKeySet()) {
                    conf.put(key, SerializerUtils.toJsonString(node.getAttribute(key, null)));
                }
                componentConfMap.put(node.getName(), conf);
                componentMap.put(node.getName(), component);
            }
            for (Party party : dag.getParties()) {
                role.putIfAbsent(party.getRole(), new ArrayList<>());
                role.get(party.getRole()).add(party.getName());
                Map<String, String> componentAttr = new HashMap<>();
                for (PartyNode node : party.getNodes()) {
                    Map<String, Object> attr = node.getAttributes();
                    componentAttr.put(node.getName(), SerializerUtils.toJsonString(attr));
                }
                if (party.getRole().equalsIgnoreCase("host")) {
                    hostConf.put(party.getName(), componentAttr);
                }
                if (party.getRole().equalsIgnoreCase("guest")) {
                    guestConf.put(party.getName(), componentAttr);
                }
            }
            Dsl dsl = new Dsl();
            dsl.setComponents(componentMap);
            DslConf dslConf = new DslConf();
            dslConf.setDsl_version("2");
            ComponentParameters componentParameters = new ComponentParameters();
            componentParameters.setCommon(componentConfMap);
            RoleConf roleConf = new RoleConf();
            roleConf.setHost(hostConf);
            roleConf.setGuest(guestConf);
            componentParameters.setRole(roleConf);
            dslConf.setComponent_parameters(componentParameters);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return new FateDAG();
    }

    public Graph translate(FateDAG dag) {
        Graph graph = new Graph();
        List<Node> nodeList = new ArrayList<>();
        List<Party> partyList = new ArrayList<>();
        graph.setNodes(nodeList);
        graph.setParties(partyList);
        Dsl dsl = dag.getDsl();
        Node pre = null;
        Map<String, Node> nodeMap = new HashMap<>();
        Map<String, Party> partyMap = new HashMap<>();
        for (Map.Entry<String, Component> entity : dsl.getComponents().entrySet()) {
            String key = entity.getKey();
            Component component = entity.getValue();
            Node node = new Node();
            node.setName(key);
            node.setType(component.getModule());
            node.setInputs(transInput(component.getInput()));
            node.setOutputs(transOutput(component.getOutput()));
            if (pre != null) {
                node.setParent(pre);
            }
            pre = node;
            nodeList.add(node);
            nodeMap.put(key, node);
        }
        DslConf dslConf = dag.getConf();
        for (Map.Entry<String, List<String>> roleEntity : dslConf.getRole().entrySet()) {
            String role = roleEntity.getKey();
            for (String partyId : roleEntity.getValue()) {
                Party party = new Party();
                party.setName(partyId);
                party.setRole(role);
                party.setNodes(new ArrayList<>());
                partyList.add(party);
                partyMap.put(role + partyId, party);
            }
        }
        ComponentParameters componentParameters = dslConf.getComponent_parameters();
        for (Map.Entry<String, Map<String, String>> attrMap : componentParameters.getCommon().entrySet()) {
            String componentName = attrMap.getKey();
            Map<String, String> attr = attrMap.getValue();
            Node node = nodeMap.get(componentName);
            if (node == null) {
                throw new RuntimeException("dsl conf component not match");
            }
            attr.forEach(node::setAttribute);
        }
        for (Map.Entry<String, Map<String, String>> guestAttr : componentParameters.getRole().getGuest().entrySet()) {
            String partyId = guestAttr.getKey();
            Map<String, String> componentsAttrs = guestAttr.getValue();
            Party party = partyMap.get("guest" + partyId);
            if (party == null) {
                throw new RuntimeException("dsl conf role not match");
            }
            for (Map.Entry<String, String> componentEntry : componentsAttrs.entrySet()) {
                String componentName = componentEntry.getKey();
                String componentAttr = componentEntry.getValue();
                PartyNode partyNode = new PartyNode();
                partyNode.setParent(party);
                partyNode.setName(componentName);
                Map<String, String> attrMap;
                try {
                    attrMap = SerializerUtils.mapObjectDeserialize(componentAttr, String.class);
                    attrMap.forEach(partyNode::setAttribute);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
                party.getNodes().add(partyNode);
            }
        }
        for (Map.Entry<String, Map<String, String>> guestAttr : componentParameters.getRole().getHost().entrySet()) {
            String partyId = guestAttr.getKey();
            Map<String, String> componentsAttrs = guestAttr.getValue();
            Party party = partyMap.get("host" + partyId);
            if (party == null) {
                throw new RuntimeException("dsl conf role not match");
            }
            for (Map.Entry<String, String> componentEntry : componentsAttrs.entrySet()) {
                String componentName = componentEntry.getKey();
                String componentAttr = componentEntry.getValue();
                PartyNode partyNode = new PartyNode();
                partyNode.setParent(party);
                partyNode.setName(componentName);
                Map<String, String> attrMap;
                try {
                    attrMap = SerializerUtils.mapObjectDeserialize(componentAttr, String.class);
                    attrMap.forEach(partyNode::setAttribute);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
                party.getNodes().add(partyNode);
            }
        }
        return graph;
    }

    private Map<String, String> transInput(Input input) {
        try {
            Map<String, String> map = new HashMap<>();
            map.put("data", SerializerUtils.toJsonString(input.getData()));
            map.put("model", SerializerUtils.toJsonString(input.getModel()));
            map.put("cache", SerializerUtils.toJsonString(input.getCache()));
            return map;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private Input transInput(Map<String, String> map) {
        try {
            String json = SerializerUtils.toJsonString(map);
            return SerializerUtils.deserialize(json, Input.class);
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
            String json = SerializerUtils.toJsonString(map);
            return SerializerUtils.deserialize(json, Output.class);
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
