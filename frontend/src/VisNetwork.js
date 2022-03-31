import Graph from "react-graph-vis";
import {useDispatch, useSelector} from "react-redux";
import {generateGraphAsync, selectEdge, selectGraph, selectNode} from "./store/graphSlice";
import {useMemo, useState} from "react";
import {v4 as uuidv4} from 'uuid';
import {PlusIcon} from "@heroicons/react/solid";

const options = {
    edges: {
        color: "#000000"
    },
};

export default function VisNetwork() {

    const dispatch = useDispatch();

    const events = {
        select: function (event) {
            const {nodes, edges} = event;
            console.log(nodes, edges);

            if (nodes.length > 0) {
                const node = nodes[0];
                dispatch(selectNode(node))
                return
            }
            if (edges.length > 0) {
                const edge = edges[0];
                dispatch(selectEdge(edge))
                return
            }

            // Nothing selected
            dispatch(selectEdge(null))
        }
    };

    const graph = useSelector(selectGraph)

    const nodeCount = graph.nodes.length
    const edgeCount = graph.edges.length
    const nodeIds = graph.nodes.map((x) => x.id).join(',')
    const edgeIds = graph.edges.map((x) => x.id).join(',')

    const [network, setNetwork] = useState()
    const [nodes, setNodes] = useState()
    const [edges, setEdges] = useState()



    const visNetworkChild = useMemo(() => <Graph
        key={uuidv4()}
        graph={graph}
        options={options}
        events={events}
        getNetwork={network => {
            setNetwork(network)
            //  if you want access to vis.js network api you can set the state in a parent component using this property
        }}
        getNodes={nodes => {
            setNodes(nodes)
        }}
        getEdges={edges => {
            setEdges(edges)
        }}
    />, [nodeCount, edgeCount,nodeIds,edgeIds]);

    if (nodes != null) {
        nodes.update(graph.nodes)
    }
    if (edges != null) {
        edges.update(graph.edges)
    }


    return (
        <div className="h-full w-full absolute flex flex-col justify-center items-center">
            {graph.nodes.length > 0 &&
            visNetworkChild
            }
            {graph.nodes.length === 0 &&
            <div className="text-center">
                <svg
                    className="mx-auto h-12 w-12 text-gray-400"
                    fill="none"
                    viewBox="0 0 24 24"
                    stroke="currentColor"
                    aria-hidden="true"
                >
                    <path
                        vectorEffect="non-scaling-stroke"
                        strokeLinecap="round"
                        strokeLinejoin="round"
                        strokeWidth={2}
                        d="M9 13h6m-3-3v6m-9 1V7a2 2 0 012-2h6l2 2h6a2 2 0 012 2v8a2 2 0 01-2 2H5a2 2 0 01-2-2z"
                    />
                </svg>
                <h3 className="mt-2 text-sm font-medium text-gray-900">No graph</h3>
                <p className="mt-1 text-sm text-gray-500">Get started by creating a new graph.</p>
                <div className="mt-6">
                    <button
                        onClick={() => dispatch(generateGraphAsync())}
                        type="button"
                        className="inline-flex items-center px-4 py-2 border border-transparent shadow-sm text-sm font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
                    >
                        <PlusIcon className="-ml-1 mr-2 h-5 w-5" aria-hidden="true" />
                        New Graph
                    </button>
                </div>
            </div>
            }
        </div>
    );
}