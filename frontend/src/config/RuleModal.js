/* This example requires Tailwind CSS v2.0+ */
import {Fragment, useRef, useState} from 'react'
import {Dialog, Transition} from '@headlessui/react'
import {ExclamationIcon} from '@heroicons/react/outline'
import {useSelector} from "react-redux";
import {genRules} from "../store/graphSlice";
import {XIcon} from "@heroicons/react/solid";

function classNames(...classes) {
    return classes.filter(Boolean).join(' ')
}

export default function RuleModal({open, setOpen}) {

    const cancelButtonRef = useRef(null)
    const generatedRules = useSelector(genRules)

    const tabs = Object.keys(generatedRules)

    const [currentTab, setCurrentTab] = useState(0)

    return (
        <Transition.Root show={open} as={Fragment}>
            <Dialog as="div" className="fixed z-10 inset-0 overflow-y-auto" initialFocus={cancelButtonRef}
                    onClose={setOpen}>
                <div className="flex items-end justify-center min-h-screen pt-4 px-4 pb-20 text-center block">
                    <Transition.Child
                        as={Fragment}
                        enter="ease-out duration-300"
                        enterFrom="opacity-0"
                        enterTo="opacity-100"
                        leave="ease-in duration-200"
                        leaveFrom="opacity-100"
                        leaveTo="opacity-0"
                    >
                        <Dialog.Overlay className="fixed inset-0 bg-gray-500 bg-opacity-75 transition-opacity"/>
                    </Transition.Child>

                    {/* This element is to trick the browser into centering the modal contents. */}
                    <span className="hidden inline-block align-middle h-screen" aria-hidden="true">
            &#8203;
          </span>
                    <Transition.Child
                        as={Fragment}
                        enter="ease-out duration-300"
                        enterFrom="opacity-0 translate-y-4 translate-y-0 scale-95"
                        enterTo="opacity-100 translate-y-0 scale-100"
                        leave="ease-in duration-200"
                        leaveFrom="opacity-100 translate-y-0 scale-100"
                        leaveTo="opacity-0 translate-y-4 translate-y-0 scale-95"
                    >
                        <div
                            className="inline-block align-bottom bg-white rounded-lg px-4 pt-5 pb-4 text-left overflow-hidden shadow-xl transform transition-all my-8 align-middle w-full p-6">
                            <div className="block absolute top-0 right-0 pt-4 pr-4">
                                <button
                                    type="button"
                                    className="bg-white rounded-md text-gray-400 hover:text-gray-500 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500"
                                    onClick={() => setOpen(false)}
                                >
                                    <span className="sr-only">Close</span>
                                    <XIcon className="h-6 w-6" aria-hidden="true" />
                                </button>
                            </div>
                            <div className="flex items-start">
                                <div className="mt-3 mt-0 text-left w-full">
                                    <Dialog.Title as="h3" className="text-lg leading-6 font-medium text-gray-900">
                                        Generated Rules
                                    </Dialog.Title>
                                    <div className="mt-2">
                                        <div>
                                            <div className="block">
                                                <div className="border-b border-gray-200">
                                                    <nav className="-mb-px flex space-x-8" aria-label="Tabs">
                                                        {tabs.map((tab, index) => (
                                                            <button
                                                                key={tab}
                                                                onClick={() => setCurrentTab(index)}
                                                                className={classNames(
                                                                    index === currentTab
                                                                        ? 'border-indigo-500 text-indigo-600'
                                                                        : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300',
                                                                    'whitespace-nowrap py-4 px-1 border-b-2 font-medium text-sm'
                                                                )}
                                                            >
                                                                {tab}
                                                            </button>
                                                        ))}
                                                    </nav>
                                                </div>
                                            </div>
                                        </div>

                                        <pre className="overflow-x-scroll mt-5 text-sm text-gray-500">{generatedRules[tabs[currentTab]]}</pre>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </Transition.Child>
                </div>
            </Dialog>
        </Transition.Root>
    )
}