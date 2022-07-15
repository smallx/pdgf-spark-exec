# PDGF Spark Exec
Execute PDGF on top of Spark

## Build

```shell
sbt package
```

## Usage

```shell
export PDGF_HOME=/dir/to/pdgf
spark-submit --jars "$PDGF_HOME/pdgf.jar, $PDGF_HOME/extlib/*" --files "$PDGF_HOME/dicts/*" target/scala-2.11/pdgf-spark-exec_2.11-1.0.1.jar [PDGF OPTIONS]
```

### Example - TPCx-AI
Generate the training data sets for customer and CUSTOMER_IMAGES using the TPCx-AI data generator

```shell
export PDGF_HOME="$TPCXAI_HOME/lib/pdgf"
spark-submit --jars "$PDGF_HOME/pdgf.jar,$PDGF_HOME/extlib/*" --files "$PDGF_HOME/dicts/*" target/scala-2.11/pdgf-spark-exec_2.11-1.0.1.jar -ns -sp MY_SEED 1234.0 -sf 1 -sp includeLabels 1.0 -sp TTVF 1.0 -s customer  
```