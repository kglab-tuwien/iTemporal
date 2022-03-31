import Edge from "./edge/Edge";
import Node from "./node/Node";
import {useSelector} from "react-redux";
import {selectedEdge, selectedNode} from "./store/graphSlice";

export default function EditView() {
    const selectEdge = useSelector(selectedEdge)
    const selectNode = useSelector(selectedNode)

    return (
        <>
            {selectEdge &&
            <Edge/>
            }
            {selectNode &&
            <Node/>
            }
        </>
    )
}