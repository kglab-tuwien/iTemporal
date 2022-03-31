import VisNetwork from "./VisNetwork";
import ConfigPane from "./ConfigPane";
import EditView from "./EditView";
import {useDispatch} from "react-redux";
import {storeFile} from "./store/graphSlice";
import LoadState from "./LoadState";

export default function App() {

    const dispatch = useDispatch();

    return (
        <>
            {/* Background color split screen for large screens */}
            <div className="fixed top-0 left-0 w-1/2 h-full bg-white" aria-hidden="true"/>
            <div className="fixed top-0 right-0 w-1/2 h-full bg-gray-50" aria-hidden="true"/>
            <div className="relative min-h-screen flex flex-col">

                {/* Navbar */}
                <nav className="flex-shrink-0 bg-blue-500">
                    <div className="mx-auto px-2 sm:px-4 lg:px-8">
                        <div className="relative flex items-center justify-between h-16">
                            {/* Logo section */}
                            <div className="flex items-center px-2 lg:px-0">
                                <div className="flex-shrink-0">
                                    <h1 className="text-2xl font-bold leading-7 text-white sm:text-3xl sm:truncate">iTemporal
                                        - Temporal
                                        Benchmark Generator</h1>
                                </div>

                            </div>
                            <div>
                                <button
                                    className="bg-white py-2 px-3 border border-gray-300 rounded-md shadow-sm text-sm leading-4 font-medium text-gray-700 hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500"
                                    onClick={() => dispatch(storeFile())}>Store</button>
                                <LoadState/>
                            </div>
                        </div>
                    </div>
                </nav>

                {/* 3 column wrapper */}
                <div className="flex-grow w-full mx-auto flex flex-col-reverse lg:flex-row ">
                    {/* Left sidebar & main wrapper */}
                    <div className="flex-1 min-w-0 bg-white flex flex-col ">


                        <div className="bg-white lg:min-w-0 lg:flex-1">
                            <div className="h-full py-6 px-4 sm:px-6 lg:px-8">
                                {/* Start main area*/}
                                <div className="relative h-full" style={{minHeight: '36rem'}}>

                                    <div
                                        className="absolute inset-0 border-2 border-gray-200 border-dashed rounded-lg"/>
                                    <VisNetwork/>

                                </div>
                                {/* End main area */}
                            </div>
                        </div>

                        <div
                            className="border-gray-200 bg-white">
                            <div className="h-full pl-4 pr-6 py-6 sm:pl-6 lg:pl-8">
                                {/* Start edit area */}
                                <div className="h-full relative" style={{minHeight: '12rem'}}>
                                    <div
                                        className="absolute inset-0 border-2 border-gray-200 border-dashed rounded-lg"/>
                                    <div className={"relative"}>
                                        <EditView/>
                                    </div>

                                </div>
                                {/* End edit area */}
                            </div>
                        </div>
                    </div>

                    <div
                        className="bg-gray-50 pr-4 sm:pr-6 lg:pr-8 lg:flex-shrink-0 lg:border-l lg:border-gray-200">
                        <div className="h-full pl-6 py-6 lg:w-80">
                            {/* Start generator config area */}
                            <div className="h-full relative" style={{minHeight: '16rem'}}>
                                <div>
                                    <ConfigPane/>
                                </div>
                            </div>
                            {/* End generator config area */}
                        </div>
                    </div>
                </div>
            </div>
        </>
    )
}