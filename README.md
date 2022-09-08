# PDGF Spark Exec
Execute PDGF on top of Spark

## Build

```shell
mkdir -p lib
cp /path/to/pdgf.jar lib/
sbt package
```

## Usage (Example for TPCx-BB 30TB)

add the following configuration to `core-site.xml` or `hdfs-site.xml`, to disable hadoop filesystem cache:

```xml
<property>
    <name>fs.hdfs.impl.disable.cache</name>
    <value>true</value>
</property>
```

then:

```shell
cd $TPCXBB_HOME/data-generator/dicts
tar -zcvf dicts.tar.gz *
mv dicts.tar.gz ../dicts.tar.gz

export PDGF_HOME=$TPCXBB_HOME/data-generator
export SPARK_HOME=/path/to/spark
bin/spark-submit \
  --master yarn \
  --deploy-mode cluster \
  --conf 'spark.driver.memory=3g' \
  --conf 'spark.driver.memoryOverhead=1g' \
  --conf 'spark.executor.instances=1000' \
  --conf 'spark.executor.cores=3' \
  --conf 'spark.task.cpus=3' \
  --conf 'spark.executor.memory=500m' \
  --conf 'spark.executor.memoryOverhead=1500m' \
  --jars "$PDGF_HOME/pdgf.jar,$PDGF_HOME/extlib/*.jar" \
  --archives "$PDGF_HOME/dicts.tar.gz#dicts" \
  --files "$PDGF_HOME/Constants.properties,$SPARK_HOME/conf/core-site.xml" \
  --class de.bankmark.pdgf.SparkExec \
  pdgf-spark-exec_2.12-1.0.1.jar \
  JAVA_BIN_PATH -Xmx1g -Djava.awt.headless=true -Dcore-site.xml=core-site.xml -Dhdfs-site.xml= -DFileChannelProvider=pdgf.util.caching.fileWriter.HDFSChannelProvider -Ddfs.replication.override=1 -cp "./*:./__spark_libs__/*" pdgf.Controller -ns -c -sp REFRESH_PHASE 0 -o "'hdfs://host:port/path/to/tpcxbb/benchmarks/bigbench/data/'+table.getName()+'/'" -ap 3000 -s -sf 30000
```