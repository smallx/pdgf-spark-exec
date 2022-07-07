# PDGF Spark Exec
Execute PDGF on top of Spark

## Build

```shell
sbt package
```

## Usage

```shell
export PDGF_HOME=/dir/to/pdgf
spark-submit --jars "$PDGF_HOME/pdgf.jar, $PDGF_HOME/extlib/*" --files "$PDGF_HOME/dicts/*" [PDGF OPTIONS]
```

### Example - TPCx-AI
Generate the training data sets for customer and CUSTOMER_IMAGES using the TPCx-AI data generator

```shell
export PDGF_HOME="$TPCXAI_HOME/lib/pdgf"
spark-submit --jars "$PDGF_HOME/pdgf.jar, $PDGF_HOME/extlib/*" --files "$PDGF_HOME/dicts/*" -ns -sp MY_SEED 1234 -sf 1 -sp includeLabels 1.0 -sp TTVF 1.0 -s customer CUSTOMER_IMAGES
```