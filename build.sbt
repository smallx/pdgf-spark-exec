name := "pdgf-spark-exec"
version := "1.0.1"
organization := "de.bankmark.pdgf"

//crossScalaVersions := Seq("2.11", "2.12", "2.13")
scalaVersion := "2.11.12"

val sparkVersion = "2.4.5"
val spark3Version = "3.3.0"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % sparkVersion,
  "org.apache.spark" %% "spark-sql" % sparkVersion,
//    excludeAll(ExclusionRule(organization = "org.javassist", name = "javassist"))
  "org.javassist" % "javassist" % "3.20.0-GA",
  "org.rogach" %% "scallop" % "4.1.0"
)
