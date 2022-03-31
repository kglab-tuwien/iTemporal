# iTemporal 

iTemporal is an easily extensible, open-source temporal benchmark generator written in Kotlin.

Its core features are the support of generating DatalogMTL-compliant programs with an additional support of aggregation.

With the usage of a dynamic registration system of the core, the generator is designed to be extensible with additional features.

The details about the graph generator can be found in the corresponding paper published at ICDE 2022:

Luigi Bellomarini, Markus Nissl, and Emanuel Sallinger, "iTemporal: An extensible Temporal Benchmark Generator", ICDE 2022, [to appear](https://...)

## Execution 

In order to execute the program, we recommend adapting the main program and set the desired properties.
All possible properties can be found in the Properties class in the util package.

You find the main execution file and function in:
```
at.ac.tuwien.dbai.kg.iTemporal -> Main.kt -> main()
```
## Source Code:

The source code packages contain the following content:

| package | description |
|---------|-------------|
| aggregation | This package contains the aggregation module. This is, the different aggregation edges with their assignment logic, property assignment for the edges etc.   |
| core | This packages contains the core of the generator. It contains the specification of the underlying graph structure as well as the basic Datalog nodes (linear, intersection, union)   |
| graphGenerator | This package contains the code of the graph generator for creating the underlying graph structure |
| temporal | This package contains the temporal edges. This is, the different temporal edges with their assignment logic, property assignment for the edges etc. |
| ruleGenerator | This package contains the code for generating the rules for the target platforms. |
| util | Some general helper function as well as the property file. |

