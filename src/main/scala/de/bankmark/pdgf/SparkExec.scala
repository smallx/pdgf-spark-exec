package de.bankmark.pdgf

import org.apache.spark.sql.SparkSession

import java.io.File
import scala.sys.process._

/**
 * Execute PDGF on top of Spark.
 */
object SparkExec {

  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder()
      .appName("PDGFSparkExec")
      .getOrCreate()
    val sc = spark.sparkContext

    val numExecutors = sc.getConf.getInt("spark.executor.instances", 2)
    val numCoresPerExecutor = sc.getConf.getInt("spark.executor.cores", 1)
    val taskCpus = sc.getConf.getInt("spark.task.cpus", 1)
    assert(numExecutors > 0, "spark.executor.instances must be > 0")
    assert(numCoresPerExecutor > 0, "spark.executor.cores must be > 0")
    assert(taskCpus > 0, "spark.task.cpus must be > 0")
    assert(numCoresPerExecutor == taskCpus, "requires spark.executor.cores == spark.task.cpus, one task per executor")

    // one task per executor
    val rdd = sc.parallelize(1 to numExecutors, numExecutors)

    rdd.foreach(nodeNum => {
      // prepare java on executor side
      for (i <- args.indices) {
        if (args(i).contains("JAVA_BIN_PATH")) {
          val javaHome = System.getenv("JAVA_HOME")
          if (javaHome == null || javaHome.isEmpty) {
            throw new Exception("JAVA_HOME environment is not set on executor side")
          }
          args(i) = args(i).replace("JAVA_BIN_PATH", s"$javaHome/bin/java")
        }
      }

      // prepare pdgf command
      val pdgfDistributedArgs = s"-nn $nodeNum -nc $numExecutors -w $taskCpus"
      val pdgfCmd = (args ++ pdgfDistributedArgs.split(" ")).toList
      println(s"run pdgf: ${pdgfCmd.mkString(" ")}")

      // run pdgf process & wait finished
      val startTime = System.currentTimeMillis()
      val exitValue = Process(pdgfCmd, new File("."), "CLASSPATH" -> "")  // clear CLASSPATH environment
        .run()
        .exitValue()
      val endTime = System.currentTimeMillis()

      // print & check exit code
      println(s"pdgf finished with exit code $exitValue, duration ${(endTime - startTime) / 1000} s")
      if (exitValue != 0) {
        throw new Exception(s"pdgf failed with exit code $exitValue")
      }
    })

    println("all done")
  }
}
