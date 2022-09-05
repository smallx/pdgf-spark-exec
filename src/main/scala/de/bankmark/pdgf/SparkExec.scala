package de.bankmark.pdgf

import javassist.{ClassClassPath, ClassPool}
import org.apache.spark.SparkContext
import org.apache.spark.sql.SparkSession
import pdgf.Controller

object SparkExec {

  def currentActiveExecutorCount(sc: SparkContext): Int = {
    val allExecutors = sc.getExecutorMemoryStatus.keys
    val driverHost = sc.getConf.get("spark.driver.host")
    if (allExecutors.exists(_.split(":")(0) == driverHost)) {
      allExecutors.size - 1
    } else {
      allExecutors.size
    }
  }

  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder()
      .appName("PDGFSparkExec")
      .getOrCreate()
    val sparkContext = spark.sparkContext

    val numExecutors = sparkContext.getConf.getInt("spark.executor.instances", -1)
    assert(numExecutors > 0, "spark.executor.instances must be > 0")

    // wait for all executors to be ready, these are the nodes used by pdgf
    // one pdgf instance : one spark task : one spark executor
    val waitStartTime = System.currentTimeMillis()
    var numActiveExecutors = currentActiveExecutorCount(sparkContext)
    while (numActiveExecutors < numExecutors && System.currentTimeMillis() - waitStartTime < 10 * 60 * 1000) {
      Thread.sleep(1000)
      numActiveExecutors = currentActiveExecutorCount(sparkContext)
      println(s"current executors: ${numActiveExecutors} / ${numExecutors}, wait time: ${(System.currentTimeMillis() - waitStartTime) / 1000} s")
    }

    val numCoresPerExecutor = sparkContext.getConf.getInt("spark.executor.cores", -1)
    val numNodes = numActiveExecutors

    // create an RDD with #`numNodes` elements
    val rdd = sparkContext.parallelize(1 to numActiveExecutors, numNodes)
    // on each partition run a pdgf instance and set the number of workers, i.e. the threads spawned by pdgf accordingly
    rdd.foreach(executorNum => {
      val startTime = System.currentTimeMillis()

      // make the PDGF parameters for distributed data generation
      val workers = if (numCoresPerExecutor > 0) numCoresPerExecutor else Runtime.getRuntime.availableProcessors()
      val pdgfDistributedArgs = s"-nn $executorNum -nc $numNodes -w $workers"
      val pdgfArgs = args ++ pdgfDistributedArgs.split(" ")

      // add PDGF to javassist classpool
      // when running on Spark, PDGF is not part of the system classpath but the Spark classpath
      // hence its classes cannot be found by javassist
      ClassPool.getDefault.appendClassPath(new ClassClassPath(classOf[Controller]))

      // call pdgf with the command line args + the distributed args
      Controller.main(pdgfArgs)

      // waiting for pdgf finished
      while (Controller.getInstance().getExitCode == null) {
        Thread.sleep(3000)
        println(s"waiting for pdgf finished, duration ${(System.currentTimeMillis() - startTime) / 1000} s")
      }
      println(s"pdgf finished with exit code ${Controller.getInstance().getExitCode}")
    })
  }
}
