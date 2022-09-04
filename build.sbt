name := "pdgf-spark-exec"
version := "1.0.1"
organization := "de.bankmark.pdgf"

scalaVersion := "2.12.15"

val sparkVersion = "2.4.5"
val spark3Version = "3.3.0"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % spark3Version % "provided",
  "org.apache.spark" %% "spark-sql" % spark3Version % "provided",
)

assembly / assemblyMergeStrategy := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x => MergeStrategy.first
}
