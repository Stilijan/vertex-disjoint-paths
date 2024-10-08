# Vertex-Disjoint Paths

A Java implementation of the proposed algorithm in this <a href="https://dl.acm.org/doi/10.5555/313852.314072">scientific paper</a> of Broder et. al.

## Requirements

- Java 21+
- Maven 3.9.5+
- WSL (for Windows users)

## Execution commands

To generate the graph files in `inputs/rands`, type:

```shell
$ ./graph-gen.sh
```
or 
```shell
$ bash ./graph-gen.sh
```
if you are on Windows with WSL. This will generate `.gr` files into the `./inputs/` folder.

To compile the project, type:

```shell
$ mvn install
```
To execute the algorithm, type:

```shell
$ mvn exec:java -Dexec.args="<path-to-gr-file> <number-pairs> <mode> [<iterations>]"
```

For the third argument you can either choose `DEFAULT_MODE` or `BENCHMARK_MODE`. Optionally, if you want to benchmark the algorithm, 
you can specify a fourth argument for the benchmarking iterations. Note that if you are on Powershell, you must type single quotes `''` around
the `-Dexec.args="..."` argument.

