import {useDispatch, useSelector} from "react-redux";
import {selectedNode, selectGraphInternal, setSelection} from "../store/graphSlice";

export default function Node() {
    const selectNode = useSelector(selectedNode)
    const graph = useSelector(selectGraphInternal)
    const dispatch = useDispatch()

    const nodes = graph.nodes.filter((x) => x.name === selectNode)


    if (nodes.length === 0) {
        return (<div>No node selected</div>)
    }

    const node = nodes[0]

    return (
        <>
            <div className="p-8 grid sm:grid-cols-2 xl:grid-cols-3 gap-8">
                <div>
                    <label htmlFor="name" className="block text-sm font-medium text-gray-700">
                        Name
                    </label>
                    <div className="mt-1">
                        <input
                            type="text"
                            name="name"
                            value={node.name}
                            id="name"
                            readOnly
                            disabled={"disabled"}
                            className="opacity-50 shadow-sm focus:ring-blue-500 focus:border-blue-500 block w-full sm:text-sm border-gray-300 rounded-md"
                            placeholder="1"
                        />
                    </div>
                </div>
                <div className={"hidden sm:col-span-1 xl:col-span-2"}></div>
                <div>
                    <label htmlFor="minArity" className="block text-sm font-medium text-gray-700">
                        Minimum Arity
                    </label>
                    <div className="mt-1">
                        <input
                            type="number"
                            name="minArity"
                            id="minArity"
                            value={node.minArity}
                            className="shadow-sm focus:ring-blue-500 focus:border-blue-500 block w-full sm:text-sm border-gray-300 rounded-md"
                            placeholder="1"
                            onChange={(event) => {
                                const newValue = parseInt(event.target.value)
                                if (isNaN(newValue)) {
                                    dispatch(setSelection({key: 'minArity', value: -1}))
                                } else {
                                    dispatch(setSelection({key: 'minArity', value: parseInt(event.target.value)}))
                                }
                            }}
                        />
                    </div>
                </div>
                <div>
                    <label htmlFor="maxArity" className="block text-sm font-medium text-gray-700">
                        Maximum Arity
                    </label>
                    <div className="mt-1">
                        <input
                            type="number"
                            name="maxArity"
                            id="maxArity"
                            value={node.maxArity}
                            className="shadow-sm focus:ring-blue-500 focus:border-blue-500 block w-full sm:text-sm border-gray-300 rounded-md"
                            placeholder="1"
                            onChange={(event) => {
                                const newValue = parseInt(event.target.value)
                                if (isNaN(newValue)) {
                                    dispatch(setSelection({key: 'maxArity', value: -1}))
                                } else {
                                    dispatch(setSelection({key: 'maxArity', value: parseInt(event.target.value)}))
                                }
                            }}
                        />
                    </div>
                </div>
            </div>
        </>
    )
}