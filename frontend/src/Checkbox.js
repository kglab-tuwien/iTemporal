export default function Checkbox({
                                     id, label = "", value = false, onChange = (values) => {
    }
                                 }) {

    return (
        <div className="relative flex items-start">
            <div className="flex items-center h-5">
                <input
                    id={id}
                    aria-describedby="candidates-description"
                    name={id}
                    type="checkbox"
                    onChange={(event) => onChange(!value)}
                    checked={value}
                    className="focus:ring-blue-500 h-4 w-4 text-blue-600 border-gray-300 rounded"
                />
            </div>
            <div className="ml-3 text-sm">
                <label htmlFor={id} className="font-medium text-gray-700">
                    {label}
                </label>
            </div>
        </div>)
}