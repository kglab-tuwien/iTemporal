import Slider from "../Slider";
import {useDispatch, useSelector} from "react-redux";
import {generatePropertiesAsync, generateRuleTypesAsync, selectProperties, setProperty} from "../store/graphSlice";

export default function RuleTypeConfig() {

    const properties = useSelector(selectProperties)
    const dispatch = useDispatch();

    function updateRuleDistributionSingle(values) {
        dispatch(setProperty({property: "coreSingleEdgeRules", value: values[0]}))
        dispatch(setProperty({property: "singleEdgeTemporalRules", value: values[1]-values[0]}))
        dispatch(setProperty({property: "aggregationRules", value: 1-values[1]}))
    }

    function updateRuleDistributionMulti(values) {
        dispatch(setProperty({property: "coreMultiEdgeRules", value: values[0]}))
        dispatch(setProperty({property: "multiEdgeTemporalRules", value: 1-values[0]}))
    }

    function updateCoreMulti(values) {
        dispatch(setProperty({property: "intersectionRules", value: values[0]}))
        dispatch(setProperty({property: "unionRules", value: 1-values[0]}))
    }

    function updateTemporalMulti(values) {
        dispatch(setProperty({property: "sinceRules", value: values[0]}))
        dispatch(setProperty({property: "untilRules", value: 1-values[0]}))
    }

    function updateTemporalSingle(values) {
        dispatch(setProperty({property: "diamondMinusRules", value: values[0]}))
        dispatch(setProperty({property: "boxMinusRules", value: values[1]-values[0]}))
        dispatch(setProperty({property: "diamondPlusRules", value: values[2]-values[1]}))
        dispatch(setProperty({property: "boxPlusRules", value: 1-values[2]}))
    }



    function updateAggregationSingle(values) {
        dispatch(setProperty({property: "instantaneousTemporalAggregationRules", value: values[0]}))
        dispatch(setProperty({property: "movingWindowTemporalAggregationRules", value: values[1]-values[0]}))
        dispatch(setProperty({property: "spanningTemporalAggregationRules", value: 1-values[1]}))
    }


    const ruleDistributionSingle = [
        properties.coreSingleEdgeRules,
        properties.coreSingleEdgeRules+properties.singleEdgeTemporalRules,
        // + aggregationRules
    ]

    const ruleDistributionMulti = [
        properties.coreMultiEdgeRules
        // + multiEdgeTemporalRules
    ]

    const temporalSingle = [
        properties.diamondMinusRules,
        properties.diamondMinusRules + properties.boxMinusRules,
        properties.diamondMinusRules + properties.boxMinusRules + properties.diamondPlusRules,
        // + boxPlusRules
    ]

    const aggregationSingle = [
        properties.instantaneousTemporalAggregationRules,
        properties.instantaneousTemporalAggregationRules + properties.movingWindowTemporalAggregationRules,
        // + spanning
    ]

    return (
        <>
            <h2  className={"text-lg leading-6 font-medium text-gray-900 mb-2"}>Rule Assignment</h2>
            <Slider id="ruleCategoryDistributionSingle" label={"Core (S)/Temporal (S)/Aggr"}
                    min={0}
                    max={1}
                    step={0.01}
                    values={ruleDistributionSingle}
                    onChange={(value) => updateRuleDistributionSingle(value)}
            />
            <Slider id="ruleCategoryDistributionMulti" label={"Core (M)/Temporal(M)"}
                    min={0}
                    max={1}
                    step={0.01}
                    values={ruleDistributionMulti}
                    onChange={(value) => updateRuleDistributionMulti(value)}
            />
            <Slider id="coreMultiDistribution" label={"Int./Union"} min={0} max={1} step={0.01}
                    values={[properties.intersectionRules]}
                    onChange={(value) => updateCoreMulti(value)}
            />
            <Slider id="temporalSingleDistribution" label={"<->/[-]/<+>/[+]"} min={0} max={1} step={0.01}
                    values={temporalSingle}
                    onChange={(value) => updateTemporalSingle(value)}
            />
            <Slider id="temporalMultiDistribution" label={"Since/Until"} min={0} max={1} step={0.01}
                    values={[properties.sinceRules]}
                    onChange={(value) => updateTemporalMulti(value)}
            />
            <Slider id="aggregationSingleDistribution" label={"ITA/MWTA/STA"} min={0} max={1} step={0.01}
                    values={aggregationSingle}
                    onChange={(value) => updateAggregationSingle(value)}
            />
            <button
                className="mt-5 inline-flex items-center px-3 py-2 border border-transparent text-sm leading-4 font-medium rounded-md shadow-sm text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
                onClick={() => dispatch(generateRuleTypesAsync())}>
                Generate Rule Types
            </button>
        </>
    )
}