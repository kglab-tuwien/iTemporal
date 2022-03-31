import {useRef, useState} from "react";
import {Range, useThumbOverlap} from "react-range";

const ThumbLabel = ({
                        rangeRef,
                        values,
                        index,
    step
                    }) => {
    const [labelValue, style] = useThumbOverlap(rangeRef, values, index, step);
    return (
        <div
            data-label={index}
            className={"absolute bg-blue-100 text-blue-800 inline-flex items-baseline px-2.5 py-0.5 rounded-full text-sm font-medium mt-5 left-1/2 -translate-x-1/2"}
            style={{
                whiteSpace: 'nowrap',
                ...(style)
            }}
        >
            {labelValue}
        </div>
    );
};

export default function Slider({id, label = "", min = 1, max = 100, step = 1, values = [min], onChange = (values) => {}}) {
    const rangeRef = useRef();

    return (
        <div>
            <label htmlFor={id} className="block text-sm font-medium text-gray-700">
                {label}
            </label>
            <div className={"flex flex-row items-center"}>
                <div className={"pl-0 pr-2 py-2 text-sm font-medium text-gray-700 text-left"}>{min}</div>
                <div className={"grow p-2"}>
                    <Range
                        id={id}
                        step={step}
                        min={min}
                        max={max}
                        ref={rangeRef}
                        values={values}
                        onChange={(values) => onChange(values)}
                        renderTrack={({props, children}) => (
                            <div
                                onMouseDown={props.onMouseDown}
                                onTouchStart={props.onTouchStart}
                                className={"h-4 w-full"}
                                style={{
                                    ...props.style,
                                    display: 'flex',
                                }}
                            >
                                <div
                                    ref={props.ref}
                                    className="bg-gray-200"
                                    style={{
                                        height: '5px',
                                        width: '100%',
                                        borderRadius: '4px',
                                        alignSelf: 'center'
                                    }}
                                >
                                    {children}
                                </div>
                            </div>
                        )}
                        renderThumb={({props, isDragged, index}) => (
                            <div
                                {...props}
                                className={"h-5 w-5 border rounded-full relative" + (isDragged ? ' bg-blue-700' : ' bg-blue-600')}
                                style={{
                                    ...props.style,
                                }}
                            >
                                {isDragged && <ThumbLabel
                                    rangeRef={rangeRef.current}
                                    values={values}
                                    index={index}
                                    step={step}
                                />}
                            </div>
                        )}
                    />
                </div>
                <div className={"pr-0 py-2 pl-2 text-sm font-medium text-gray-700"}>{max}</div>
            </div>
        </div>
    )
}