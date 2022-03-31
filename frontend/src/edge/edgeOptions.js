export const OPTION_FROM = 0; // NodeID, not changeable
export const OPTION_TO = 1; // NodeID, not changeable
export const OPTION_TERM_ORDER = 2; // Array of Integers
export const OPTION_JOIN_OVERLAPPING_TERMS = 3; // Integer
export const OPTION_JOIN_NON_OVERLAPPING_TERMS = 4; // Integer
export const OPTION_TEMPORAL_INTERVAL = 5; // Integer
export const OPTION_TEMPORAL_FIRST_PREDICATE = 6; // For since and until
export const OPTION_TRIANGLE_UP_UNIT = 7; // tor triangle up operator year, month, ...
export const OPTION_AGGREGATION_GROUP_BY_TERMS = 8; // how many group by are there
export const OPTION_AGGREGATION_CONTRIBUTOR_TERMS = 9; // how many contributors are there
export const OPTION_AGGREGATION_AGGREGATION_TYPE = 10; // which type of aggregation is executed

export const triangleUnitOptions = [
    {
        title: 'Unknown',
        description: 'The generator selects a random mapping for you.',
        current: false,
    },
    {

        title: 'Year',
        description: 'Maps interval to a year granularity',
        current: false,
    },
    {

        title: 'Month',
        description: 'Maps interval to a month granularity',
        current: false,
    },
    {

        title: 'Week',
        description: 'Maps interval to a week granularity',
        current: false,
    },
    {

        title: 'Day',
        description: 'Maps interval to a day granularity',
        current: false,
    },
]

export const aggregationTypeOptions = [
    {

        title: 'Unknown',
        description: 'The generator selects a random mapping for you.',
        current: false,
        hasContributor: false,
    },
    {

        title: 'Max',
        description: 'The aggreagtion computes the maximum',
        current: false,
        hasContributor: false,
    },
    {

        title: 'Min',
        description: 'The aggreagtion computes the minimum',
        current: false,
        hasContributor: true,
    },
    {

        title: 'Count',
        description: 'The aggreagtion computes the number of elements',
        current: false,
        hasContributor: true,
    },
    {

        title: 'Sum',
        description: 'The aggreagtion computes the sum of the elements',
        current: false,
    },
]

export const edgeOptions = [
    /*{
        title: 'GenericEdge',
        description: 'The most generic edge. Each edge is a subtype of this edge. The generator will assign a random edge',
        current: true,
        multi: false,
        editable: true,
        options: [
            OPTION_FROM,
            OPTION_TO,
            OPTION_TERM_ORDER
        ]
    },*/
    {
        title: 'GenericMultiEdge',
        description: 'This group contains all edge types that require more than one incoming edge. The generator will assign a random multi-edge',
        current: false,
        multi: true,
        editable: true,
        options: [
            OPTION_FROM,
            OPTION_TO,
            OPTION_TERM_ORDER
        ]
    },
    {
        title: 'GenericSingleEdge',
        description: 'This group contains all edge types that require one incoming edge. The generator will assing a random single-edge.',
        current: false,
        multi: false,
        editable: true,
        options: [
            OPTION_FROM,
            OPTION_TO,
            OPTION_TERM_ORDER
        ]
    },
    {
        title: 'GenericCoreMultiEdge',
        description: 'This group contains all multi-edges from the core module.',
        current: false,
        multi: true,
        editable: true,
        options: [
            OPTION_FROM,
            OPTION_TO,
            OPTION_TERM_ORDER
        ]
    },
    {
        title: 'GenericCoreSingleEdge',
        description: 'This group contains all single-edges from the core module.',
        current: false,
        multi: false,
        editable: true,
        options: [
            OPTION_FROM,
            OPTION_TO,
            OPTION_TERM_ORDER
        ]
    },
    {
        title: 'GenericTemporalSingleEdge',
        description: 'This group contains all multi-edges from the temporal module.',
        current: false,
        multi: false,
        editable: true,
        options: [
            OPTION_FROM,
            OPTION_TO,
            OPTION_TERM_ORDER,
            OPTION_TEMPORAL_INTERVAL,
        ]
    },
    {
        title: 'GenericTemporalMultiEdge',
        description: 'This group contains all single-edges from the temporal module.',
        current: false,
        multi: true,
        editable: true,
        options: [
            OPTION_FROM,
            OPTION_TO,
            OPTION_TERM_ORDER,
            OPTION_TEMPORAL_INTERVAL,
        ]
    },
    {
        title: 'GenericAggregationEdge',
        description: 'This group contains all single-edges from the aggregation module.',
        current: false,
        multi: false,
        editable: true,
        options: [
            OPTION_FROM,
            OPTION_TO,
            OPTION_TERM_ORDER,
            OPTION_AGGREGATION_GROUP_BY_TERMS,
            OPTION_AGGREGATION_CONTRIBUTOR_TERMS,
            OPTION_AGGREGATION_AGGREGATION_TYPE,
        ]
    },
    {
        title: 'IntersectionEdge',
        description: '(Core-M) This edge intersects with another edge that is incoming to the target node.',
        current: false,
        multi: true,
        editable: true,
        options: [
            OPTION_FROM,
            OPTION_TO,
            OPTION_TERM_ORDER,
            OPTION_JOIN_OVERLAPPING_TERMS,
            OPTION_JOIN_NON_OVERLAPPING_TERMS,
        ]
    },
    {
        title: 'UnionEdge',
        description: '(Core-M) This edge computes the union with anote edge the is incoming to the target node.',
        current: false,
        multi: true,
        editable: true,
        options: [
            OPTION_FROM,
            OPTION_TO,
            OPTION_TERM_ORDER
        ]
    },
    {
        title: 'LinearEdge',
        description: '(Core-S) This edge is the only incoming edge and has no special property',
        current: false,
        multi: false,
        editable: true,
        options: [
            OPTION_FROM,
            OPTION_TO,
            OPTION_TERM_ORDER
        ]
    },
    {
        title: 'DiamondMinusEdge',
        description: '(Temporal-S) This edge match the logic of the diamond-minus operator of DatalogMTL',
        current: false,
        multi: false,
        editable: true,
        options: [
            OPTION_FROM,
            OPTION_TO,
            OPTION_TERM_ORDER,
            OPTION_TEMPORAL_INTERVAL,
        ]
    },
    {
        title: 'DiamondPlusEdge',
        description: '(Temporal-S) This edge match the logic of the diamond-plus operator of DatalogMTL',
        current: false,
        multi: false,
        editable: true,
        options: [
            OPTION_FROM,
            OPTION_TO,
            OPTION_TERM_ORDER,
            OPTION_TEMPORAL_INTERVAL,
        ]
    },
    {
        title: 'BoxMinusEdge',
        description: '(Temporal-S) This edge match the logic of the box-minus operator of DatalogMTL',
        current: false,
        multi: false,
        editable: true,
        options: [
            OPTION_FROM,
            OPTION_TO,
            OPTION_TERM_ORDER,
            OPTION_TEMPORAL_INTERVAL,
        ]
    },
    {
        title: 'BoxPlusEdge',
        description: '(Temporal-S) This edge match the logic of the box-plus operator of DatalogMTL',
        current: false,
        multi: false,
        editable: true,
        options: [
            OPTION_FROM,
            OPTION_TO,
            OPTION_TERM_ORDER,
            OPTION_TEMPORAL_INTERVAL,
        ]
    },
    {
        title: 'ClosingEdge',
        description: '(Temporal-S) This edge closes the intervals.',
        current: false,
        multi: false,
        editable: false,
        options: [
            OPTION_FROM,
            OPTION_TO,
            OPTION_TERM_ORDER
        ]
    },
    {
        title: 'SinceEdge',
        description: '(Temporal-M) This edge computes the since-operator with another edge that is incoming to the target node.',
        current: false,
        multi: true,
        editable: true,
        options: [
            OPTION_FROM,
            OPTION_TO,
            OPTION_TERM_ORDER,
            OPTION_TEMPORAL_INTERVAL,
            OPTION_TEMPORAL_FIRST_PREDICATE
        ]
    },
    {
        title: 'UntilEdge',
        description: '(Temporal-M) This edge computes the until-operator with another edge that is incoming to the target node.',
        current: false,
        multi: true,
        editable: true,
        options: [
            OPTION_FROM,
            OPTION_TO,
            OPTION_TERM_ORDER,
            OPTION_TEMPORAL_INTERVAL,
            OPTION_TEMPORAL_FIRST_PREDICATE
        ]
    },
    {
        title: 'TriangleUpEdge',
        description: '(Temporal-S) This edge rounds the interval to a higher granularity (e.g. -> seconds to hours)',
        current: false,
        multi: false,
        editable: false,
        options: [
            OPTION_FROM,
            OPTION_TO,
            OPTION_TERM_ORDER,
            OPTION_TRIANGLE_UP_UNIT
        ]
    },
    {
        title: 'ITAEdge',
        description: '(Aggregation-S) This edge match the default aggregation per timepoint',
        current: false,
        multi: false,
        editable: true,
        options: [
            OPTION_FROM,
            OPTION_TO,
            OPTION_TERM_ORDER,
            OPTION_AGGREGATION_GROUP_BY_TERMS,
            OPTION_AGGREGATION_CONTRIBUTOR_TERMS,
            OPTION_AGGREGATION_AGGREGATION_TYPE,
        ]
    },
    {
        title: 'MWTAEdge',
        description: '(Aggregation-S) This edge match the aggregation for a moving window',
        current: false,
        multi: false,
        editable: true,
        options: [
            OPTION_FROM,
            OPTION_TO,
            OPTION_TERM_ORDER,
            OPTION_AGGREGATION_GROUP_BY_TERMS,
            OPTION_AGGREGATION_CONTRIBUTOR_TERMS,
            OPTION_AGGREGATION_AGGREGATION_TYPE,
            OPTION_TEMPORAL_INTERVAL
        ]
    },
    {
        title: 'STAEdge',
        description: '(Aggregation-S) This edge match the aggregation for a spanning window',
        current: false,
        multi: false,
        editable: true,
        options: [
            OPTION_FROM,
            OPTION_TO,
            OPTION_TERM_ORDER,
            OPTION_TRIANGLE_UP_UNIT,
            OPTION_AGGREGATION_GROUP_BY_TERMS,
            OPTION_AGGREGATION_CONTRIBUTOR_TERMS,
            OPTION_AGGREGATION_AGGREGATION_TYPE,
        ]
    },
]