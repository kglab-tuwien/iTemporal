import Slider from "../Slider";
import {useDispatch, useSelector} from "react-redux";
import {generateGraphAsync, generatePropertiesAsync, selectProperties, setProperty} from "../store/graphSlice";

export default function PropertyConfig() {

    const properties = useSelector(selectProperties)
    const dispatch = useDispatch();


    return (
        <>
            <h2 className={"text-lg leading-6 font-medium text-gray-900 mb-2"}>Properties</h2>
            <Slider
                id="temporalFactor"
                label={"Interval Multiplier (1000=s)"}
                min={1000}
                max={1000000}
                step={1000}
                values={[properties.temporalFactor]}
                onChange={(values) => dispatch(setProperty({property: "temporalFactor", value: values[0]}))}
            />
            <Slider
                id="temporalUnits"
                label={"Average Temporal Interval [a,b]"}
                min={0}
                max={100}
                step={1}
                values={[properties.averageNumberOfTemporalUnitsT1, properties.averageNumberOfTemporalUnitsT2]}
                onChange={(values) => {
                    dispatch(setProperty({property: "averageNumberOfTemporalUnitsT1", value: values[0]}))
                    dispatch(setProperty({property: "averageNumberOfTemporalUnitsT2", value: values[1]}))
                }}
            />
            <Slider
                id="varianceTemporalUnitsA"
                label={"Variance in Temporal Unit a"}
                min={0}
                max={10}
                step={0.1}
                values={[properties.varianceNumberOfTemporalUnitsT1]}
                onChange={(values) => dispatch(setProperty({property: "varianceNumberOfTemporalUnitsT1", value: values[0]}))}
            />
            <Slider
                id="varianceTemporalUnitsB"
                label={"Variance in Temporal Unit b"}
                min={0}
                max={10}
                step={0.1}
                values={[properties.varianceNumberOfTemporalUnitsT2]}
                onChange={(values) => dispatch(setProperty({property: "varianceNumberOfTemporalUnitsT2", value: values[0]}))}
            />
            <Slider
                id="temporalMaxPrecision"
                label={"Number of decimals in temporal units"}
                min={0}
                max={5}
                step={1}
                values={[properties.temporalMaxPrecision]}
                onChange={(values) => dispatch(setProperty({property: "temporalMaxPrecision", value: values[0]}))}
            />
            <button
                className="mt-5 inline-flex items-center px-3 py-2 border border-transparent text-sm leading-4 font-medium rounded-md shadow-sm text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
                onClick={() => dispatch(generatePropertiesAsync())}>
                Generate Properties
            </button>
        </>
    )
}