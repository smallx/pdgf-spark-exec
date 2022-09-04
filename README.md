# PDGF Spark Exec
Execute PDGF on top of Spark

## Build

```shell
mkdir -p lib
cp /path/to/pdgf.jar lib/
sbt package
```

## Usage (Example for TPCx-BB 30TB)

```shell
cd $TPCXBB_HOME/data-generator/dicts
tar -zcvf dicts.tar.gz *
mv dicts.tar.gz ../dicts.tar.gz

export PDGF_HOME=$TPCXBB_HOME/data-generator
export SPARK_HOME=/path/to/spark
bin/spark-submit \
  --master yarn \
  --deploy-mode client \
  --conf 'spark.executor.instances=1000' \
  --conf 'spark.executor.cores=3' \
  --conf 'spark.executor.memory=3g' \
  --conf 'spark.executor.memoryOverhead=512m' \
  --conf 'spark.driver.userClassPathFirst=true' \
  --conf 'spark.executor.userClassPathFirst=true' \
  --conf 'spark.hadoop.fs.hdfs.impl.disable.cache=true' \
  --conf 'spark.executor.extraJavaOptions=-Djava.awt.headless=true -Dcore-site.xml=core-site.xml -Dhdfs-site.xml= -DFileChannelProvider=pdgf.util.caching.fileWriter.HDFSChannelProvider -Ddfs.replication.override=1' \
  --jars "$PDGF_HOME/pdgf.jar,$PDGF_HOME/extlib/*.jar" \
  --archives "$PDGF_HOME/dicts.tar.gz#dicts" \
  --files "$PDGF_HOME/Constants.properties,$SPARK_HOME/conf/core-site.xml" \
  pdgf-spark-exec_2.12-1.0.1.jar \
  -ns -c -sp REFRESH_PHASE 0 -o "'hdfs://host:port/path/to/tpcxbb/benchmarks/bigbench/data/'+table.getName()+'/'" -ap 3000 -s -sf 30000
```