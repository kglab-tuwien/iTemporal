import {useDispatch} from "react-redux";
import {loadState} from "./store/graphSlice";

export default function LoadState() {

    const dispatch = useDispatch();

    const onFileChange = (event) => {
        const file = event.target.files[0]

        const reader = new FileReader();
        reader.addEventListener('load', (event) => {
            const data = event.target.result;
            dispatch(loadState(JSON.parse(data)))
        });
        reader.readAsText(file);
        event.target.value = "";

    }

    return (
        <label htmlFor="load-config-upload"
               className="ml-3 cursor-pointer bg-white py-2 px-3 border border-gray-300 rounded-md shadow-sm text-sm leading-4 font-medium text-gray-700 hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500">
            <span>Load</span>
            <input id="load-config-upload" name="load-config-upload" type="file" className="sr-only" accept=".json"
                   onChange={(event) => onFileChange(event)}
            />
        </label>
    )
}