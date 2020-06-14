name := "exchange-platform"

version := "0.1"

scalaVersion := "2.11.12"

def commonSettings = Seq(
  scalaVersion := "2.11.8",
  version := "1.0",
  assemblyMergeStrategy in assembly := {
    case x => MergeStrategy.last
  }
)

libraryDependencies += "org.apache.spark" %% "spark-core" % "2.4.5" % "provided"
libraryDependencies += "org.apache.spark" %% "spark-sql" % "2.4.5" % "provided"
libraryDependencies += "org.apache.spark" %% "spark-streaming" % "2.4.5" % "provided"
libraryDependencies += "org.apache.spark" %% "spark-streaming-kafka-0-10" % "2.4.5" % "provided"
libraryDependencies += "org.apache.spark" %% "spark-sql-kafka-0-10" % "2.4.5"
libraryDependencies += "ru.yandex.clickhouse" % "clickhouse-jdbc" % "0.2.4"

//assemblyMergeStrategy in assembly := {
//  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
//  case x => MergeStrategy.first
//}

//libraryDependencies += "org.apache.spark" %% "spark-streaming-kafka-0-10" % "2.4.5" excludeAll(
//  ExclusionRule(organization = "org.spark-project.spark", name = "unused"),
//  ExclusionRule(organization = "org.apache.hadoop"),
//  ExclusionRule(organization = "org.apache.spark", name = "spark-streaming")
//)