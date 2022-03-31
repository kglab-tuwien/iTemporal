import Slider from "../Slider";
import {useDispatch, useSelector} from "react-redux";
import {
    downloadData,
    downloadRules,
    genData,
    generateDataAsync,
    generatePropertiesAsync,
    generateRulesAsync, genRules,
    selectProperties,
    setProperty
} from "../store/graphSlice";
import {useState} from "react";
import RuleModal from "./RuleModal";
import DataModal from "./DataModal";
import Checkbox from "../Checkbox";

export default function DataConfig() {
    const properties = useSelector(selectProperties)
    const generatedRules = useSelector(genRules)
    const generatedData = useSelector(genData)

    const dispatch = useDispatch();

    const [showRuleModal, setShowRuleModal] = useState(false)
    const [showDataModal, setShowDataModal] = useState(false)

    return (
        <>
            <h2 className={"text-lg leading-6 font-medium text-gray-900 mb-2"}>Data Generation</h2>
            <Slider
                id="cardinalityTermDomain"
                label={"Cardinality of Domain"}
                min={100}
                max={1000}
                step={1}
                values={[properties.cardinalityTermDomain]}
                onChange={(values) => dispatch(setProperty({property: "cardinalityTermDomain", value: values[0]}))}
            />
            <Slider id="averageAmountOfGeneratedOutputs"
                    label={"Average Number of Seeding Output Tuples"}
                    min={10}
                    max={1000}
                    step={10}
                    values={[properties.averageAmountOfGeneratedOutputs]}
                    onChange={(values) => dispatch(setProperty({property: "averageAmountOfGeneratedOutputs", value: values[0]}))}
            />
            <Slider id="varianceAmountOfGeneratedOutputs"
                    label={"Variance Number of Seeing Output Tuples"}
                    min={0}
                    max={10}
                    step={0.1}
                    values={[properties.varianceAmountOfGeneratedOutputs]}
                    onChange={(values) => dispatch(setProperty({property: "varianceAmountOfGeneratedOutputs", value: values[0]}))}
            />
            <Slider id="outputIntervalStartRange"
                    label={"Start Range Output Interval"}
                    min={1577836800}
                    max={1640995200}
                    step={60}
                    values={[properties.outputTimestampStart/1000, properties.outputTimestampEnd/1000]}
                    onChange={(values) => {
                        dispatch(setProperty({property: "outputTimestampStart", value: values[0]*1000}))
                        dispatch(setProperty({property: "outputTimestampEnd", value: values[1]*1000}))
                    }
                    }
                    initValues={[1577836800, 1640995200]}
            />
            <Slider id="averageOutputIntervalDuration"
                    label={"Average Interval Length"}
                    min={1}
                    max={20}
                    values={[properties.averageOutputIntervalDuration]}
                    onChange={(values) => dispatch(setProperty({property: "averageOutputIntervalDuration", value: values[0]}))}
            />
            <Slider id="varianceOutputIntervalDuration" label={"Variance Interval Length"}
                    min={0}
                    max={10}
                    step={0.1}
                    values={[properties.varianceOutputIntervalDuration]}
                    onChange={(values) => dispatch(setProperty({property: "varianceOutputIntervalDuration", value: values[0]}))}
            />
            <Slider id="averageAggregationSelectivity" label={"Aggregation Selectivity"}
                    min={0}
                    max={1}
                    step={0.01}
                    values={[properties.averageAggregationSelectivity]}
                    onChange={(values) => dispatch(setProperty({property: "averageAggregationSelectivity", value: values[0]}))}
            />
            <Slider id="varianceAggregationSelectivity" label={"Variance Aggregation Selectivity"}
                    min={0.0}
                    max={0.1}
                    step={0.01}
                    values={[properties.varianceAggregationSelectivity]}
                    onChange={(values) => dispatch(setProperty({property: "varianceAggregationSelectivity", value: values[0]}))}
            />
            <Slider id="unionInclusionPercentage" label={"Tuples included in union"}
                    min={0.0}
                    max={1.0}
                    step={0.01}
                    initValues={[0.6]}
                    values={[properties.unionInclusionPercentage]}
                    onChange={(values) => dispatch(setProperty({property: "unionInclusionPercentage", value: values[0]}))}
            />
            <Slider id="temporalInclusionPercentage" label={"Tuples forwarded in temporal cycles"}
                    min={0.0}
                    max={1}
                    step={0.01}
                    initValues={[0.6]}
                    values={[properties.temporalInclusionPercentage]}
                    onChange={(values) => dispatch(setProperty({property: "temporalInclusionPercentage", value: values[0]}))}
            />

            <Checkbox
                id="generateTimePoints"
                label={"Generate Time Points"}
                value={properties.generateTimePoints}
                onChange={(value) => dispatch(setProperty({property: "generateTimePoints", value: value}))}
            />
            <div className="grid grid-cols-2 mt-5 gap-4">
            <button
                className="inline-flex items-center text-center px-3 py-2 border border-transparent text-sm leading-4 font-medium rounded-md shadow-sm text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
                onClick={() => dispatch(generateRulesAsync())}>
                Generate Rules
            </button>
            <button
                className="inline-flex items-center  text-center px-3 py-2 border border-transparent text-sm leading-4 font-medium rounded-md shadow-sm text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
                onClick={() => dispatch(generateDataAsync())}>
                Generate Data
            </button>
                {Object.keys(generatedRules).length > 0 ? <button
                    className="inline-flex items-center  text-center px-3 py-2 border border-transparent text-sm leading-4 font-medium rounded-md shadow-sm text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
                    onClick={() => setShowRuleModal(true)}>
                    Show Rules
                </button> : <div/>}
                {Object.keys(generatedData).length > 0 ? <button
                    className="inline-flex items-center  text-center px-3 py-2 border border-transparent text-sm leading-4 font-medium rounded-md shadow-sm text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
                    onClick={() => setShowDataModal(true)}>
                    Show Data
                </button> : <div/>}
                {Object.keys(generatedRules).length > 0 ? <button
                    className="inline-flex items-center  text-center px-3 py-2 border border-transparent text-sm leading-4 font-medium rounded-md shadow-sm text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
                    onClick={() => dispatch(downloadRules())}>
                    Download Rules
                </button> : <div/>}
                {Object.keys(generatedData).length > 0 ? <button
                    className="inline-flex items-center  text-center px-3 py-2 border border-transparent text-sm leading-4 font-medium rounded-md shadow-sm text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
                    onClick={() => dispatch(downloadData())}>
                    Download Data
                </button> : <div/>}
            </div>
            <RuleModal open={showRuleModal} setOpen={(value) => setShowRuleModal(value)}/>
            <DataModal open={showDataModal} setOpen={(value) => setShowDataModal(value)}/>

            {/*
                    var generateTimePoints:Boolean = true,
                    var averageAggregationBucket:Double = 1.0,
                    var varianceAggregationBucket:Double = 0.0,
                    var percentageViaContributor:Double = 0.7,

                    var outputCsvHeader:Boolean = false,
                    var outputQuestDB: Boolean = false
                    */}
        </>
    )
}