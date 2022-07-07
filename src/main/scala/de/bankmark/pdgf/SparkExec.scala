package de.bankmark.pdgf

import org.apache.spark.SparkContext
import org.apache.spark.sql.SparkSession
import pdgf.ui.cli.Controller
import scalismo.support.nativelibs.NativeLibraryException

import java.lang.Thread.sleep

object SparkExec {

  /** Method that just returns the current active/registered executors
   * excluding the driver.
   * @param sc The spark context to retrieve registered executors.
   * @return a list of executors each in the form of host:port.
   */
  def currentActiveExecutors(sc: SparkContext, includeDriver: Boolean = true): Seq[String] = {
    val allExecutors = sc.getExecutorMemoryStatus.keys
    val driverHost = sc.getConf.get("spark.driver.host");
    if (includeDriver) {
      allExecutors.toSeq
    } else {
      allExecutors.filter(!_.split(":")(0).equals(driverHost)).toSeq
    }
  }

  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder()
      .appName("PDGFSparkExec")
      .getOrCreate()
    val sparkContext = spark.sparkContext

    // get the current active executors
    val executors = currentActiveExecutors(sparkContext, true)
    val numCoresPerExecutor = sparkContext.getConf.getInt("spark.executor.cores", -1)
    val numNodes = executors.length
    println(s"found $numNodes executors:")
    println(executors.mkString(","))

    // create an RDD with #`numNodes` elements
    val rdd = sparkContext.parallelize(executors, numNodes)
    // on each partition run a pdgf instance and set the number of workers, i.e. the threads spawned by pdgf accordingly
    val datagen = rdd.zipWithIndex().map(executorWithIdx => {
      val executor = executorWithIdx._1
      val executorNum = executorWithIdx._2 + 1
      val workers =  if (numCoresPerExecutor > 0) numCoresPerExecutor else Runtime.getRuntime.availableProcessors()
      val pdgfDistributedArgs = s"-nc $executorNum -nn $numNodes -w $workers"
      val pdgfArgs = args ++ pdgfDistributedArgs.split(" ")
      val prettyArgs = pdgfArgs.map("\"" + _ + "\"").mkString("Array(", ",", ")")
      println(s"run pdgf on $executor: Controller.main(${prettyArgs})")
      Controller.main(pdgfArgs)
    })

    val results = datagen.collect()
    println("RESULTS")
    results.foreach(println)
  }
}