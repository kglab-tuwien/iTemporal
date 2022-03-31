import Select from "./Select";
import {Switch} from '@headlessui/react'
import {useState} from "react";
import {
    aggregationTypeOptions,
    edgeOptions,
    OPTION_AGGREGATION_AGGREGATION_TYPE,
    OPTION_AGGREGATION_CONTRIBUTOR_TERMS,
    OPTION_AGGREGATION_GROUP_BY_TERMS,
    OPTION_FROM,
    OPTION_JOIN_NON_OVERLAPPING_TERMS,
    OPTION_JOIN_OVERLAPPING_TERMS,
    OPTION_TEMPORAL_FIRST_PREDICATE,
    OPTION_TEMPORAL_INTERVAL,
    OPTION_TERM_ORDER,
    OPTION_TO,
    OPTION_TRIANGLE_UP_UNIT,
    triangleUnitOptions
} from "./edgeOptions";
import {useDispatch, useSelector} from "react-redux";
import {selectedEdge, selectedNode, selectGraphInternal, setSelection} from "../store/graphSlice";
import {DebounceInput} from "react-debounce-input";

function classNames(...classes) {
    return classes.filter(Boolean).join(' ')
}

export default function Edge() {
    const dispatch = useDispatch()
    const selectEdge = useSelector(selectedEdge)
    const graph = useSelector(selectGraphInternal)

    const edges = graph.edges.filter((x) => x.uniqueId === selectEdge)


    if (edges.length === 0) {
        return (<div>No edge selected</div>)
    }

    const edge = edges[0]

    const edgeOption = edgeOptions.filter((x) => x.title === edge.type)[0]
    const triangleUnit = triangleUnitOptions.filter((x) => x.title === edge.unit)[0]
    const aggregationType = aggregationTypeOptions.filter((x) => x.title === edge.aggregationType)[0]
    const enabledEdgeSettings = edgeOption['options']

    const enabledEdgeOptions = edgeOptions.filter((x) => x.multi === edgeOption.multi)

    return (
        <div className="p-8 grid sm:grid-cols-2 xl:grid-cols-3 gap-8">
            <div className={"sm:col-span-2 xl:col-span-1 row-span-2"}>
                <Select data={enabledEdgeOptions} value={edgeOption} onChange={(value) => dispatch(setSelection({key:"type", value:value.title}))} label={"Edge Type"}/>
            </div>

            {enabledEdgeSettings.indexOf(OPTION_FROM) !== -1 &&
            <div>
                <label htmlFor="fromNode" className="block text-sm font-medium text-gray-700">
                    From Node
                </label>
                <div className="mt-1">
                    <input
                        type="text"
                        name="fromNode"
                        id="fromNode"
                        readOnly
                        disabled={"disabled"}
                        className="opacity-50 shadow-sm focus:ring-blue-500 focus:border-blue-500 block w-full sm:text-sm border-gray-300 rounded-md"
                        placeholder="n1"
                        value={edge.from}
                    />
                </div>
            </div>
            }
            {enabledEdgeSettings.indexOf(OPTION_TO) !== -1 &&
            <div>
                <label htmlFor="toNode" className="block text-sm font-medium text-gray-700">
                    To Node
                </label>
                <div className="mt-1">
                    <input
                        type="text"
                        name="toNode"
                        id="toNode"
                        readOnly
                        disabled={"disabled"}
                        className="opacity-50 shadow-sm focus:ring-blue-500 focus:border-blue-500 block w-full sm:text-sm border-gray-300 rounded-md"
                        placeholder="n2"
                        value={edge.to}
                    />
                </div>
            </div>
            }
            {enabledEdgeSettings.indexOf(OPTION_TERM_ORDER) !== -1 &&
            <div>
                <label htmlFor="termOrder" className="block text-sm font-medium text-gray-700">
                    Term Order
                </label>
                <div className="mt-1">
                    <DebounceInput
                        debounceTimeout={500}
                        type="text"
                        name="termOrder"
                        id="termOrder"
                        className="shadow-sm focus:ring-blue-500 focus:border-blue-500 block w-full sm:text-sm border-gray-300 rounded-md"
                        placeholder="1,2,3,4"
                        value={edge.termOrder.join(", ")}
                        onChange={(event) => {dispatch(setSelection({key: 'termOrder', value: event.target.value.split(",").map((x) => parseInt(x.trim())).filter((x) => !isNaN(x))}))}}
                    />
                </div>
            </div>
            }
            <div className={"hidden sm:block"}></div>
            {enabledEdgeSettings.indexOf(OPTION_JOIN_OVERLAPPING_TERMS) !== -1 &&
            <>
                <div className={"hidden xl:block"}></div>
                <div>
                    <label htmlFor="numberOfOverlappingJoinTuples" className="block text-sm font-medium text-gray-700">
                        Number Of Overlapping Tuples (Join)
                    </label>
                    <div className="mt-1">
                        <input
                            type="number"
                            name="numberOfOverlappingJoinTuples"
                            id="numberOfOverlappingJoinTuples"
                            className="shadow-sm focus:ring-blue-500 focus:border-blue-500 block w-full sm:text-sm border-gray-300 rounded-md"
                            placeholder="2"
                            value={edge.overlappingTerms}
                            onChange={(event) => {dispatch(setSelection({key: 'overlappingTerms', value: event.target.value}))}}
                        />
                    </div>
                </div>
            </>
            }
            {enabledEdgeSettings.indexOf(OPTION_JOIN_NON_OVERLAPPING_TERMS) !== -1 &&
            <div>
                <label htmlFor="numberOfNonOverlappingJoinTuples" className="block text-sm font-medium text-gray-700">
                    Number Of Non-Overlapping Tuples (Join)
                </label>
                <div className="mt-1">
                    <input
                        type="number"
                        name="numberOfNonOverlappingJoinTuples"
                        id="numberOfNonOverlappingJoinTuples"
                        className="shadow-sm focus:ring-blue-500 focus:border-blue-500 block w-full sm:text-sm border-gray-300 rounded-md"
                        placeholder="1"
                        value={edge.nonOverlappingTerms}
                        onChange={(event) => {dispatch(setSelection({key: 'nonOverlappingTerms', value: event.target.value}))}}
                    />
                </div>
            </div>
            }

            {enabledEdgeSettings.indexOf(OPTION_TEMPORAL_FIRST_PREDICATE) !== -1 &&
            <Switch.Group as="div" className="flex items-center">
                <Switch
                    checked={edge.isLeftEdge}
                    onChange={(value) => {dispatch(setSelection({key: 'isLeftEdge', value: value}))}}
                    className={classNames(
                        edge.isLeftEdge ? 'bg-blue-600' : 'bg-gray-200',
                        'relative inline-flex flex-shrink-0 h-6 w-11 border-2 border-transparent rounded-full cursor-pointer transition-colors ease-in-out duration-200 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500'
                    )}
                >
        <span
            aria-hidden="true"
            className={classNames(
                edge.isLeftEdge ? 'translate-x-5' : 'translate-x-0',
                'pointer-events-none inline-block h-5 w-5 rounded-full bg-white shadow transform ring-0 transition ease-in-out duration-200'
            )}
        />
                </Switch>
                <Switch.Label as="span" className="ml-3">
                    <span className="text-sm font-medium text-gray-900">is First Predicate </span>
                </Switch.Label>
            </Switch.Group>
            }

            {enabledEdgeSettings.indexOf(OPTION_TEMPORAL_INTERVAL) !== -1 &&
            <>
            {enabledEdgeSettings.indexOf(OPTION_TEMPORAL_FIRST_PREDICATE) === -1 && <div className={"hidden xl:block"}></div>}
                <div>
                    <label htmlFor="intervalStart" className="block text-sm font-medium text-gray-700">
                        Left Interval Endpoint
                    </label>
                    <div className="mt-1">
                        <input
                            type="number"
                            name="intervalStart"
                            id="intervalStart"
                            className="shadow-sm focus:ring-blue-500 focus:border-blue-500 block w-full sm:text-sm border-gray-300 rounded-md"
                            placeholder="1"
                            value={edge.t1}
                            onChange={(event) => {dispatch(setSelection({key: 't1', value: event.target.value}))}}
                        />
                    </div>
                </div>
            </>
            }
            {enabledEdgeSettings.indexOf(OPTION_TEMPORAL_INTERVAL) !== -1 &&
            <div>
                <label htmlFor="intervalEnd" className="block text-sm font-medium text-gray-700">
                    Right Interval Endpoint
                </label>
                <div className="mt-1">
                    <input
                        type="number"
                        name="intervalEnd"
                        id="intervalEnd"
                        className="shadow-sm focus:ring-blue-500 focus:border-blue-500 block w-full sm:text-sm border-gray-300 rounded-md"
                        placeholder="1"
                        value={edge.t2}
                        onChange={(event) => {dispatch(setSelection({key: 't2', value: event.target.value}))}}
                    />
                </div>
            </div>
            }

            {enabledEdgeSettings.indexOf(OPTION_TRIANGLE_UP_UNIT) !== -1 &&
            <>
                <div className={"hidden xl:block"}></div>
                <div>
                <Select
                    data={triangleUnitOptions}
                    value={triangleUnit}
                    onChange={(value) => dispatch(setSelection({key:"unit", value:value.title}))}
                        label={"Triangle Up Unit"}/>
                </div>
                <div className={"hidden xl:block"}></div>
            </>
            }



            {enabledEdgeSettings.indexOf(OPTION_AGGREGATION_AGGREGATION_TYPE) !== -1 &&
            <>
                <div>
                    <Select data={aggregationTypeOptions}
                            value={aggregationType}
                            onChange={(value) => dispatch(setSelection({key:"aggregationType", value:value.title}))}
                            label={"Aggregation Type"}/>
                </div>
                {enabledEdgeSettings.indexOf(OPTION_TRIANGLE_UP_UNIT) === -1 &&
                <div className={"hidden sm:block xl:hidden"}></div>}
            </>
            }
            {enabledEdgeSettings.indexOf(OPTION_AGGREGATION_GROUP_BY_TERMS) !== -1 &&
            <div>
                <label htmlFor="groupByTerms" className="block text-sm font-medium text-gray-700">
                    Number Of Group By Terms
                </label>
                <div className="mt-1">
                    <input
                        type="number"
                        name="groupByTerms"
                        id="groupByTerms"
                        className="shadow-sm focus:ring-blue-500 focus:border-blue-500 block w-full sm:text-sm border-gray-300 rounded-md"
                        placeholder="1"
                        value={edge.numberOfGroupingTerms}
                        onChange={(event) => {dispatch(setSelection({key: 'numberOfGroupingTerms', value: event.target.value}))}}
                    />
                </div>
            </div>
            }
            {enabledEdgeSettings.indexOf(OPTION_AGGREGATION_CONTRIBUTOR_TERMS) !== -1 &&
            <div>
                <label htmlFor="contributorTerms" className="block text-sm font-medium text-gray-700">
                    Number of Contributor Terms
                </label>
                <div className="mt-1">
                    <input
                        type="number"
                        name="contributorTerms"
                        id="contributorTerms"
                        className="shadow-sm focus:ring-blue-500 focus:border-blue-500 block w-full sm:text-sm border-gray-300 rounded-md"
                        placeholder="1"
                        value={edge.numberOfContributors}
                        onChange={(event) => {dispatch(setSelection({key: 'numberOfContributors', value: event.target.value}))}}
                    />
                </div>
            </div>
            }
        </div>
    )
}