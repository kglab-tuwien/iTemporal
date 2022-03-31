// Calls the API to get a random graph to work with
import axios from "axios";

/*const demoData = {
        nodes: [
            {
                "name": "a",
                "type": "General",
                "minArity": 0,
                "maxArity": 4,
            },
            {
                "name": "b",
                "type": "General",
                "minArity": 0,
                "maxArity": 4,
            },
            {
                "name": "c",
                "type": "General",
                "minArity": 0,
                "maxArity": 4,
            },
            {
                "name": "d",
                "type": "General",
                "minArity": 0,
                "maxArity": 4,
            }
        ],
        edges: [
            {
                "uniqueId": "e1",
                "from": "a",
                "to": "b",
                "type": "DiamondMinusEdge",
                termOrder: [],
            },
            {
                "uniqueId": "e2",
                "from": "a",
                "to": "c",
                "type": "ITAEdge",
                termOrder: [],
            },
            {
                "uniqueId": "e3",
                "from": "b",
                "to": "d",
                "type": "IntersectionEdge",
                termOrder: [],
            },
            {
                "uniqueId": "e4",
                "from": "c",
                "to": "d",
                "type": "IntersectionEdge",
                termOrder: [],
            }
        ],
    }*/

const URL = "http://localhost:8081"
export function runGenerateGraph(properties) {
    return axios.post(URL,{
        step: "NORMALIZATION",
        properties: properties,
    })
}

// Calls the API to assign to the graph specific rule types (e.g., diamond minus)
export function runRuleTypeGeneration(graph, properties) {
    return axios.post(URL,{
        step: "RULE_DECOMPOSITION",
        properties: properties,
        dependencyGraph: JSON.stringify(graph)
    })
}

// Calls the API to assign the properties to edges and nodes (e.g., arity)
export function runPropertyGeneration(graph, properties) {
    return axios.post(URL,{
        step: "PROPERTY_AFTER",
        properties: properties,
        dependencyGraph: JSON.stringify(graph)
    })
}

// Calls the API to get the resulting Datalog rules
export function runRuleGeneration(graph, properties) {
    return axios.post(URL+"/rules",{
        step: "ALL",
        properties: properties,
        dependencyGraph: JSON.stringify(graph)
    })
}

// Calls the API to get some data to run the rules with
export function runDataGeneration(graph, properties) {
    return axios.post(URL+"/data",{
        step: "ALL",
        properties: properties,
        dependencyGraph: JSON.stringify(graph)
    })
}

