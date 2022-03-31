# Benchmark Data 

This folder contains pre-generated benchmark data. 


## Benchmarks:

1. Tests performance of a spanning temporal aggregation
2. Tests performance of a moving window temporal aggregation
3. Tests performance of an instantaneous temporal aggregation
4. Tests recursive aggregation
5. Tests recursive aggregation
6. Tests performance of a since operator (until shares same characteristics)
7. Tests performance of a diamond operator
8. Tests performance of a box operator
9. Tests performance of temporal operator chaning 
10. Tests use of temporal operators in a recursive setting
11. Tests a simple mix of temporal and aggregation operators
12. Tests a more complex non-recursive mix of temporal and aggregation operators
13. Tests a small recursive example with a mix of temporal and aggregation operators (a single cycle)
14. Tests a middle recursive example with a mix of temporal and aggregation operators
15. Tests a large recursive example with a mix of temporal and aggregation operators

## Folders:

* The `config` folder contains the configuration files for the benchmarks generated with the web UI.
* The `data` folder contains the data used for the benchmark queries each subfolder targeting a specific benchmark.
* The `query` folder contains the queries of the benchmarks. One subfolder for Vadalog and one subfolder for PostgresSQL.