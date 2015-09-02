import sbt._
import sbt.Keys._
import scoverage.ScoverageSbtPlugin._
import scoverage.ScoverageSbtPlugin

object GemFireSparkConnectorBuild extends Build {
  import Settings._
  import Dependencies._ 

  lazy val connectorRoot = baseDirectory in ThisBuild

  lazy val geodeCodeSettings = Seq( 
    geodeBaseDirectory := connectorRoot.value /".."
  ) ++ buildSettings
  
  lazy val root = Project(
    id = "root", 
    base =file("."), 
    aggregate = Seq(gemfireFunctions, gemfireSparkConnector,demos),
    settings = commonSettings ++ geodeCodeSettings ++ Seq( 
     name := "GemFire Connector for Apache Spark",
     publishArtifact :=  false,
     publishLocal := { },
     publish := { },
     target := outputDir.value /"target"
    )
  )
 
  lazy val gemfireFunctions = Project(
    id = "gemfire-functions",
    base = file("gemfire-functions"),
    settings = functionSettings ++ geodeCodeSettings ++ Seq(libraryDependencies ++= Dependencies.functions,
      resolvers ++= gfcResolvers,
      target := outputDir.value /"gemfire-functions/target",
      description := "Required GemFire Functions to be deployed onto the GemFire Cluster before using the GemFire Spark Connector" ++{ printf("JASON :" + target.value)
     "HAHA"
    }
    )
  ).configs(IntegrationTest)
  
  lazy val gemfireSparkConnector = Project(
    id = "gemfire-spark-connector",
    base = file("gemfire-spark-connector"),
    settings = gfcSettings ++ geodeCodeSettings ++Seq(libraryDependencies ++= Dependencies.connector,
      resolvers ++= gfcResolvers,
      description := "A library that exposes GemFire regions as Spark RDDs, writes Spark RDDs to GemFire regions, and executes OQL queries from Spark Applications to GemFire",
      target := outputDir.value /"gemfire-spark-connector/target"
    )
  ).dependsOn(gemfireFunctions).configs(IntegrationTest)

 
  /******** Demo Project Definitions ********/ 
  lazy val demoPath = file("gemfire-spark-demos")

  lazy val demos = Project ( 
    id = "gemfire-spark-demos",
    base = demoPath,
    settings = demoSettings ++ geodeCodeSettings ++ Seq(
      target := outputDir.value /"gemfire-spark-demos/target" 
    ),
    aggregate = Seq(basicDemos)
  )
 
  lazy val basicDemos = Project (
    id = "basic-demos",
    base = demoPath / "basic-demos",
    settings = demoSettings ++ geodeCodeSettings ++ Seq(libraryDependencies ++= Dependencies.demos,
      resolvers ++= gfcResolvers,
      description := "Sample applications that demonstrates functionality of the GemFire Spark Connector",
      target := outputDir.value /"gemfire-spark-demos/basic-demos/target" 
    )
  ).dependsOn(gemfireSparkConnector)
}

