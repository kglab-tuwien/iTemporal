import {createAsyncThunk, createSlice} from '@reduxjs/toolkit';

import {
    runDataGeneration,
    runGenerateGraph,
    runPropertyGeneration,
    runRuleGeneration,
    runRuleTypeGeneration
} from "../api/GeneratorAPI";
import {edgeOptions} from "../edge/edgeOptions";

const saveFile = async (blob, fileName) => {
    const a = document.createElement('a');
    a.download = fileName;
    a.href = URL.createObjectURL(blob);
    a.addEventListener('click', (e) => {
        setTimeout(() => URL.revokeObjectURL(a.href), 30 * 1000);
    });
    a.click();
};


function transformGraph(state) {
    state.graph.nodes = []
    state.graph.edges = []
    for (const node of state.graphInternal.nodes) {
        state.graph.nodes.push({
            id: node.name,
            label: node.name + ("/" + node.minArity),
        })
    }
    for (const edge of state.graphInternal.edges) {
        state.graph.edges.push({
            id: edge.uniqueId,
            from: edge.from,
            to: edge.to,
            label: edge.type,
        })
    }
}

function enrichGraph(data) {
    const nodeProperties = [
        {
            key: 'minArity',
            value: -1,
        },
        {
            key: 'maxArity',
            value: -1,
        },
    ]

    const edgeProperties = [
        {
            key: 'overlappingTerms',
            value: -1,
        },
        {
            key: 'isLeftEdge',
            value: false,
        },
        {
            key: 'nonOverlappingTerms',
            value: -1,
        },
        {
            key: 'aggregationType',
            value: "Unknown",
        },
        {
            key: 'numberOfGroupingTerms',
            value: -1,
        },
        {
            key: 'numberOfContributors',
            value: -1,
        },
        {
            key: 'nonOverlappingTerms',
            value: -1,
        },
        {
            key: 't1',
            value: -1,
        },
        {
            key: 't2',
            value: -1,
        },
        {
            key: 'unit',
            value: "Unknown",
        },
    ]

    for (const node of data.nodes) {
        for (const nodeProperty of nodeProperties) {
            if (!(nodeProperty.key in node)) {
                node[nodeProperty.key] = nodeProperty.value
            }
        }
    }

    for (const edge of data.edges) {
        for (const edgeProperty of edgeProperties) {
            if (!(edgeProperty.key in edge)) {
                edge[edgeProperty.key] = edgeProperty.value
            }
        }
    }

    return data
}

const initState = {
    graph: {
        nodes: [],
        edges: []
    },
    graphInternal: {
        nodes: [],
        edges: []
    },
    generatedRules: {},
    generatedData: {},
    properties: {
        nodes: 6,
        inputNodes: 1,
        outputNodes: 1,
        multiEdgeRules: 0.3,
        recursiveRules: 0.1,
        recursiveComplexity: 0.1,

        coreMultiEdgeRules: 1.0,
        multiEdgeTemporalRules: 0.0,
        coreSingleEdgeRules: 0.45,
        singleEdgeTemporalRules: 0.25,
        aggregationRules: 0.3,

        // Fine-grained properties for single edge rules.
        linearRules: 1.0,

        // Fine-grained properties for multi edge rules.
        intersectionRules: 0.5,
        unionRules: 0.5,

        // Fine-grained properties for temporal rules.
        diamondMinusRules: 0.4,
        diamondPlusRules: 0.4,
        boxMinusRules: 0.1,
        boxPlusRules: 0.1,

        // Fine-grained properties for temporal multi-edge rules.
        sinceRules: 0.5,
        untilRules: 0.5,

        // Fine-grained properties for Aggregation.
        spanningTemporalAggregationRules: 0.3,
        movingWindowTemporalAggregationRules: 0.3,
        instantaneousTemporalAggregationRules: 0.4,

        // Rule arity generator (Gaussian distribution, capped at least by arity of 1)
        averageOutputArity: 3,
        varianceOutputArity: 1.0,

        // Aggregation settings
        averageNumberOfContributorTerms: 1,
        varianceNumberOfContributorTerms: 1.0,
        averageNumberOfGroupByTerms: 2,
        varianceNumberOfGroupByTerms: 1.0,

        // Intersection settings
        averageNumberOfOverlappingJoinTerms: 1,
        varianceNumberOfOverlappingJoinTerms: 1.0,

        // Temporal settings
        temporalFactor: 1000.0, // seconds = 1000
        averageNumberOfTemporalUnitsT1: 1.0,
        varianceNumberOfTemporalUnitsT1: 1.0,
        averageNumberOfTemporalUnitsT2: 3.0,
        varianceNumberOfTemporalUnitsT2: 1.0,
        temporalMaxPrecision: 0,

        // Data Generation Settings
        cardinalityTermDomain: 1000,
        averageAmountOfGeneratedOutputs: 10,
        varianceAmountOfGeneratedOutputs: 3.0,
        averageAmountOfGeneratedOutputIntervals: 1,
        varianceAmountOfGeneratedOutputIntervals: 0.0,
        outputTimestampStart: 0, //(new Date("2020-01-01")).getTime(),
        outputTimestampEnd: 1000, //(new Date("2022-01-01")).getTime(),
        averageOutputIntervalDuration: 10.0,
        varianceOutputIntervalDuration: 1.0,
        averageAggregationSelectivity: 0.2,
        varianceAggregationSelectivity: 0.02,
        unionInclusionPercentage: 0.6,
        temporalInclusionPercentage: 0.6,

        // Storage location
        path: "xxxx",

        generateTimePoints: false,

        averageAggregationBucket: 1.0,
        varianceAggregationBucket: 0.0,
        percentageViaContributor: 0.7,

        outputCsvHeader: true,
        outputQuestDB: false
    },
    selectedEdge: null,
    selectedNode: null,
}

export const generateGraphAsync = createAsyncThunk(
    'graph/generateGraph',
    async (arg, {getState}) => {
        const state = getState();
        const response = await runGenerateGraph(state.properties);
        return response.data;
    }
);

export const generateRuleTypesAsync = createAsyncThunk(
    'graph/generateRuleTypes',
    async (arg, {getState}) => {
        const state = getState();
        const response = await runRuleTypeGeneration(state.graphInternal, state.properties);
        return response.data;
    }
);

export const generatePropertiesAsync = createAsyncThunk(
    'graph/generateProperties',
    async (arg, {getState}) => {
        const state = getState();
        const response = await runPropertyGeneration(state.graphInternal, state.properties);
        return response.data;
    }
);

export const generateRulesAsync = createAsyncThunk(
    'graph/generateRules',
    async (arg, {getState}) => {
        const state = getState();
        const response = await runRuleGeneration(state.graphInternal, state.properties);
        return response.data;
    }
);

export const generateDataAsync = createAsyncThunk(
    'graph/generateData',
    async (arg, {getState}) => {
        const state = getState();
        const response = await runDataGeneration(state.graphInternal, state.properties);
        return response.data;
    }
);

const graphSlice = createSlice({
    name: 'graph',
    initialState: initState,
    reducers: {
        selectEdge: (state, action) => {
            state.selectedEdge = action.payload
            state.selectedNode = null
        },
        selectNode: (state, action) => {
            state.selectedNode = action.payload
            state.selectedEdge = null
        },
        setSelection: (state, action) => {
            if (state.selectedNode != null) {
                const node = state.graphInternal.nodes.filter((x) => x.name === state.selectedNode)[0]
                node[action.payload.key] = action.payload.value;

                switch (action.payload.key) {
                    case 'minArity':
                        node.maxArity = Math.max(node.maxArity, action.payload.value)
                        break;
                    case 'maxArity':
                        node.minArity = Math.min(node.minArity, action.payload.value)
                        break;
                    default:
                        break;
                }

            } else if (state.selectedEdge != null) {
                const edge = state.graphInternal.edges.filter((x) => x.uniqueId === state.selectedEdge)[0]
                const edgeOption = edgeOptions.filter((x) => x.title === edge.type)[0]
                edge[action.payload.key] = action.payload.value;
                if (edgeOption.multi) {
                    //const targetNode = state.graphInternal.nodes.filter((x) => x.name === edge.to)[0]
                    const otherEdge = state.graphInternal.edges.filter((x) => x.to === edge.to && edge.uniqueId !== x.uniqueId)[0]
                    switch (action.payload.key) {
                        case 'type':
                            otherEdge[action.payload.key] = action.payload.value
                            break;
                        case 'isLeftEdge':
                            otherEdge[action.payload.key] = !action.payload.value
                            break;
                        default:
                            break;
                    }
                }
            }
            transformGraph(state)
        },
        setNodes: (state, action) => {
            state.properties.nodes = action.payload.nodes;
            state.properties.inputNodes = action.payload.inputNodes;
            state.properties.outputNodes = action.payload.outputNodes;
        },
        setProperty: (state, action) => {
            state.properties[action.payload.property] = action.payload.value;
        },
        storeFile: (state) => {
            const data = {
                properties: state.properties,
                graphInternal: state.graphInternal,
            }
            const blob = new Blob([JSON.stringify(data, null, 2)], {type: 'application/json'});
            saveFile(blob, 'iTemporal.json');
        },
        loadState: (state, action) => {
            state.properties = {
                ...state.properties,
                ...action.payload.properties
            }
            state.graphInternal = action.payload.graphInternal
            state.generatedRules = {}
            state.generatedData = {}
            state.selectedEdge = null
            state.selectedNode = null
            transformGraph(state)
        },
        downloadRules: (state,action) => {
            const languages = state.generatedRules

            for (const languageKey in languages) {
                const dataBlob = new Blob([languages[languageKey]], {type: 'text/plain'})

                let fileEnding = languageKey
                if (fileEnding == "Datalog") {
                    fileEnding = "vada"
                } else if (fileEnding == "SQL") {
                    fileEnding = "sql"
                }

                saveFile(dataBlob, "rules."+fileEnding);
            }
        },
        downloadData: (state, action) => {
            const nodes = state.generatedData

            for (const nodeKey in nodes) {
                const dataBlob = new Blob([nodes[nodeKey]], {type: 'text/csv'})
                saveFile(dataBlob, nodeKey);
            }

        },
    },
    extraReducers: (builder) => {
        builder
            .addCase(generateGraphAsync.pending, (state) => {
                state.status = 'loading';
            })
            .addCase(generateRuleTypesAsync.pending, (state) => {
                state.status = 'loading';
            })
            .addCase(generatePropertiesAsync.pending, (state) => {
                state.status = 'loading';
            })
            .addCase(generateRulesAsync.pending, (state) => {
                state.status = 'loading';
            })
            .addCase(generateDataAsync.pending, (state) => {
                state.status = 'loading';
            })
            .addCase(generateGraphAsync.fulfilled, (state, action) => {
                state.status = 'idle';
                state.properties = action.payload.properties;
                const oldGraph = JSON.stringify(state.graphInternal)
                state.graphInternal = enrichGraph(JSON.parse(action.payload.graph));
                const newGraph = JSON.stringify(state.graphInternal)
                if (oldGraph !== newGraph) {
                    state.generatedRules = {}
                    state.generatedData = {}
                }
                transformGraph(state)
                state.selectedEdge = null;
                state.selectedNode = null;
            })
            .addCase(generateRuleTypesAsync.fulfilled, (state, action) => {
                state.status = 'idle';
                state.properties = action.payload.properties;
                const oldGraph = JSON.stringify(state.graphInternal)
                state.graphInternal = enrichGraph(JSON.parse(action.payload.graph));
                const newGraph = JSON.stringify(state.graphInternal)
                if (oldGraph !== newGraph) {
                    state.generatedRules = {}
                    state.generatedData = {}
                }
                transformGraph(state)
                state.selectedEdge = null;
                state.selectedNode = null;
            })
            .addCase(generatePropertiesAsync.fulfilled, (state, action) => {
                state.status = 'idle';
                state.properties = action.payload.properties;
                const oldGraph = JSON.stringify(state.graphInternal)
                state.graphInternal = enrichGraph(JSON.parse(action.payload.graph));
                const newGraph = JSON.stringify(state.graphInternal)
                if (oldGraph !== newGraph) {
                    state.generatedRules = {}
                    state.generatedData = {}
                }
                transformGraph(state)
                state.selectedEdge = null;
                state.selectedNode = null;
            })
            .addCase(generateRulesAsync.fulfilled, (state, action) => {
                state.status = 'idle';
                state.properties = action.payload.properties;
                const oldGraph = JSON.stringify(state.graphInternal)
                state.graphInternal = enrichGraph(JSON.parse(action.payload.graph));
                const newGraph = JSON.stringify(state.graphInternal)
                if (oldGraph !== newGraph) {
                    state.generatedRules = {}
                    state.generatedData = {}
                }
                transformGraph(state)
                state.selectedEdge = null;
                state.selectedNode = null;
                state.generatedRules = action.payload.rules;
            })
            .addCase(generateDataAsync.fulfilled, (state, action) => {
                state.status = 'idle';
                state.properties = action.payload.properties;
                const oldGraph = JSON.stringify(state.graphInternal)
                state.graphInternal = enrichGraph(JSON.parse(action.payload.graph));
                const newGraph = JSON.stringify(state.graphInternal)
                if (oldGraph !== newGraph) {
                    state.generatedRules = {}
                    state.generatedData = {}
                }
                transformGraph(state)
                state.selectedEdge = null;
                state.selectedNode = null;
                state.generatedData = action.payload.data;
            })
        ;
    },
})

export const {
    changeEdge,
    selectEdge,
    selectNode,
    setProperty,
    setNodes,
    setSelection,
    storeFile,
    loadState,
    downloadRules,
    downloadData
} = graphSlice.actions

export default graphSlice;

export const selectGraph = (state) => state.graph;
export const selectGraphInternal = (state) => state.graphInternal;
export const selectProperties = (state) => state.properties;
export const selectedEdge = (state) => state.selectedEdge;
export const selectedNode = (state) => state.selectedNode;
export const genRules = (state) => state.generatedRules;
export const genData = (state) => state.generatedData;