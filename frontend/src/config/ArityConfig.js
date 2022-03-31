import Slider from "../Slider";
import {useDispatch, useSelector} from "react-redux";
import {selectProperties, setProperty} from "../store/graphSlice";

export default function ArityConfig() {

    const properties = useSelector(selectProperties)
    const dispatch = useDispatch();

    return (
        <>
            <h2  className={"text-lg leading-6 font-medium text-gray-900 mb-2"}>Arity Assignment</h2>
            <Slider
                id="averageOutputArity"
                label={"Average Output arity"}
                min={1}
                max={5}
                step={1}
                values={[properties.averageOutputArity]}
                onChange={(values) => dispatch(setProperty({property: "averageOutputArity", value: values[0]}))}
            />
            <Slider
                id="varianceOutputArity"
                label={"Variance Output Arity"}
                min={0}
                max={10}
                step={0.1}
                values={[properties.varianceOutputArity]}
                onChange={(values) => dispatch(setProperty({property: "varianceOutputArity", value: values[0]}))}
            />

            <Slider
                id="averageNumberOfGroupByTerms"
                label={"Average Number of Groupby Terms"}
                min={0}
                max={5}
                values={[properties.averageNumberOfGroupByTerms]}
                onChange={(values) => dispatch(setProperty({property: "averageNumberOfGroupByTerms", value: values[0]}))}
            />
            <Slider
                id="varianceNumberOfGroupByTerms"
                label={"Variance Number of Groupby Arity"}
                min={0}
                max={10}
                step={0.1}
                values={[properties.varianceNumberOfGroupByTerms]}
                onChange={(values) => dispatch(setProperty({property: "varianceNumberOfGroupByTerms", value: values[0]}))}
            />
            <Slider
                id="averageNumberOfContributorTerms"
                label={"Average Number of Contributor Terms"}
                min={0}
                max={5}
                values={[properties.averageNumberOfContributorTerms]}
                onChange={(values) => dispatch(setProperty({property: "averageNumberOfContributorTerms", value: values[0]}))}
            />
            <Slider
                id="varianceNumberOfContributorTerms"
                label={"Variance Number of Contributor Terms"}
                min={0}
                max={10}
                step={0.1}
                values={[properties.varianceNumberOfContributorTerms]}
                onChange={(values) => dispatch(setProperty({property: "varianceNumberOfContributorTerms", value: values[0]}))}
            />
            <Slider
                id="averageNumberOfOverlappingJoinTerms"
                label={"Average Number of Overlapping Join Terms"}
                min={1}
                max={5}
                values={[properties.averageNumberOfOverlappingJoinTerms]}
                onChange={(values) => dispatch(setProperty({property: "averageNumberOfOverlappingJoinTerms", value: values[0]}))}
            />
            <Slider
                id="varianceNumberOfOverlappingJoinTerms"
                label={"Variance Number of Overlapping Join Terms"}
                min={0}
                max={10}
                step={0.1}
                values={[properties.varianceNumberOfOverlappingJoinTerms]}
                onChange={(values) => dispatch(setProperty({property: "varianceNumberOfOverlappingJoinTerms", value: values[0]}))}
            />

        </>
    )
}