import Slider from "../Slider";
import {useDispatch, useSelector} from "react-redux";
import {selectProperties, setNodes, setProperty} from "../store/graphSlice";

export default function GraphGeneratorConfig() {

    const properties = useSelector(selectProperties)
    const dispatch = useDispatch();

    const innerNodes = properties.nodes - properties.inputNodes - properties.outputNodes;

    function updateNodeProperties({inputNodes, outputNodes, innerNodes}) {

        dispatch(setNodes({
            nodes:innerNodes+inputNodes+outputNodes,
            inputNodes:inputNodes,
            outputNodes:outputNodes
        }))
    }
    return (
        <>
            <h2 className={"text-lg leading-6 font-medium text-gray-900 mb-2"}>Graph Generation</h2>
            <Slider
                id="inputNodes"
                label={"Number of Input Nodes"}
                min={1}
                max={5}
                step={1}
                values={[properties.inputNodes]}
                onChange={(value) => updateNodeProperties({inputNodes:value[0], outputNodes:properties.outputNodes, innerNodes:innerNodes})}
            />
            <Slider
                id="outputNodes"
                label={"Number of Output Nodes"}
                min={1}
                max={5}
                step={1}
                values={[properties.outputNodes]}
                onChange={(value) => updateNodeProperties({inputNodes:properties.inputNodes, outputNodes:value[0], innerNodes:innerNodes})}
            />
            <Slider
                id="innerNodes"
                label={"Number of InnerNodes"}
                min={0}
                max={20}
                step={1}
                values={[innerNodes]}
                onChange={(value) => updateNodeProperties({inputNodes:properties.inputNodes, outputNodes:properties.outputNodes, innerNodes:value[0]})}
            />
            <Slider
                id="multiEdgeRules"
                label={"Percent of multiEdgeRules"}
                min={0}
                max={1}
                step={0.01}
                values={[properties.multiEdgeRules]}
                onChange={(value) => dispatch(setProperty({property: "multiEdgeRules", value: value[0]}))}
            />
            <Slider
                id="recursiveRules"
                label={"Percent of recursiveRules"}
                min={0}
                max={1}
                step={0.01}
                values={[properties.recursiveRules]}
                onChange={(value) => dispatch(setProperty({property: "recursiveRules", value: value[0]}))}
            />
            <Slider
                id="recursiveComplexity"
                label={"Recursive Complexity"}
                min={0}
                max={1}
                step={0.01}
                values={[properties.recursiveComplexity]}
                onChange={(value) => dispatch(setProperty({property: "recursiveComplexity", value: value[0]}))}
            />
        </>
    )
}