import {generateGraphAsync, selectGraph} from "./store/graphSlice";

import {useSelector, useDispatch} from 'react-redux';
import Slider from "./Slider";
import {useState} from "react";
import {CheckIcon} from "@heroicons/react/solid";
import GraphGeneratorConfig from "./config/GraphGeneratorConfig";
import RuleTypeConfig from "./config/RuleTypeConfig";
import ArityConfig from "./config/ArityConfig";
import PropertyConfig from "./config/PropertyConfig";
import DataConfig from "./config/DataConfig";

const steps = [
    {id: 'G', name: 'Graph', description: '', href: '#'},
    {id: 'R', name: 'Rule Type', description: '', href: '#'},
    {id: 'A', name: 'Arity', description: '', href: '#'},
    {id: 'P', name: 'Property', description: '', href: '#'},
    {id: 'D', name: 'Data', description: '', href: '#'},
]

function classNames(...classes) {
    return classes.filter(Boolean).join(' ')
}


export default function ConfigPane() {

    const dispatch = useDispatch();
    const graph = useSelector(selectGraph)

    const [selectedState, setSelectedState] = useState(0);

    return (
        <div>
            <div className="flex flex-row justify-between items-end">
                <h2 className="text-xl font-medium text-gray-900">Settings</h2>
                <button
                    className="inline-flex items-center px-3 py-2 border border-transparent text-sm leading-4 font-medium rounded-md shadow-sm text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
                    onClick={() => dispatch(generateGraphAsync())}>
                    New Graph
                </button>
            </div>

            <div className="border-t border-b border-gray-200 mt-5">
                <nav aria-label="Progress">
                    <ol
                        role="list"
                        className="rounded-md overflow-hidden flex border-l border-r border-gray-200 rounded-none"
                    >
                        {steps.map((step, stepIdx) => (
                            <li key={step.id} className="relative overflow-hidden flex-1"
                                onClick={() => setSelectedState(stepIdx)}>
                                <div
                                    className={classNames(
                                        stepIdx === 0 ? 'border-b-0 rounded-t-md' : '',
                                        stepIdx === steps.length - 1 ? 'border-t-0 rounded-b-md' : '',
                                        'border border-gray-200 overflow-hidden border-0'
                                    )}
                                >
                                    {stepIdx === selectedState ? (
                                        <a href={step.href} aria-current="step">
                    <span
                        className="absolute left-0 bg-blue-600 w-full h-1 bottom-0 top-auto"
                        aria-hidden="true"
                    />
                                            <span
                                                className={classNames(
                                                    stepIdx !== 0 ? '' : '',
                                                    'px-3 py-5 flex items-start justify-center text-sm font-medium'
                                                )}
                                            >
                      <span className="flex-shrink-0">
                        <span
                            className="w-10 h-10 flex items-center justify-center border-2 border-blue-600 rounded-full">
                          <span className="text-blue-600">{step.id}</span>
                        </span>
                      </span>
                    </span>
                                        </a>
                                    ) : (
                                        <a href={step.href} className="group">
                    <span
                        className="absolute left-0 bg-transparent group-hover:bg-gray-200 w-full h-1 bottom-0 top-auto"
                        aria-hidden="true"
                    />
                                            <span
                                                className={classNames(
                                                    stepIdx !== 0 ? '' : '',
                                                    'px-3 py-5 flex items-start justify-center text-sm font-medium'
                                                )}
                                            >
                      <span className="flex-shrink-0">
                        <span
                            className="w-10 h-10 flex items-center justify-center border-2 border-gray-300 rounded-full">
                          <span className="text-gray-500">{step.id}</span>
                        </span>
                      </span>
                    </span>
                                        </a>
                                    )}

                                    {stepIdx !== 0 ? (
                                        <>
                                            {/* Separator */}
                                            <div className="absolute top-0 left-0 w-3 inset-0 block"
                                                 aria-hidden="true">
                                                <svg
                                                    className="h-full w-full text-gray-300"
                                                    viewBox="0 0 12 82"
                                                    fill="none"
                                                    preserveAspectRatio="none"
                                                >
                                                    <path d="M0.5 0V31L10.5 41L0.5 51V82" stroke="currentcolor"
                                                          vectorEffect="non-scaling-stroke"/>
                                                </svg>
                                            </div>
                                        </>
                                    ) : null}
                                </div>
                            </li>
                        ))}
                    </ol>
                </nav>
            </div>

            <div className={"mt-5"}>

            {steps[selectedState].id === 'G' && (
                <GraphGeneratorConfig/>
            )}

            {steps[selectedState].id === 'R' && (
                <RuleTypeConfig/>
            )}
            {steps[selectedState].id === 'A' && (
                <ArityConfig/>
            )}
            {steps[selectedState].id === 'P' && (
                <PropertyConfig/>
            )}
            {steps[selectedState].id === 'D' && (
                <DataConfig/>
            )}
            </div>
        </div>
    )
}