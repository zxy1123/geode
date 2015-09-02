import sbt._
import sbt.Keys._
import scala.collection.JavaConverters._
import org.scalastyle.sbt.ScalastylePlugin
import java.util.Properties

object Settings extends Build {
   
  lazy val commonSettings = Seq(
    organization := "io.pivotal",
    version := "0.5.0",
    scalaVersion := "2.10.4",
    organization := "io.pivotal.gemfire.spark",
    organizationHomepage := Some(url("http://www.pivotal.io/"))
  )

  lazy val geodeBaseDirectory = settingKey[File]("Output of geode build")
  lazy val buildProperties = settingKey[Map[String,String]]("Geode Build Properties")
  lazy val geodeBuildTargetDirectory = settingKey[File]("Geode Output Directory")
  lazy val outputDir = settingKey[File]("Connector Output Directory")

  lazy val buildSettings = Seq( 
    buildProperties := {
      val props = new Properties()
      IO.load(props, geodeBaseDirectory.value /"gradle.properties")
      props.asScala.toMap
    },
    geodeBuildTargetDirectory := file(if (buildProperties.value.getOrElse("buildRoot", "").trim.isEmpty) geodeBaseDirectory.value.getAbsolutePath else buildProperties.value.get("buildRoot").get),
    outputDir := geodeBuildTargetDirectory.value / "gemfire-spark-connector" 
  )

  lazy val gfcResolvers = Seq(
  "Repo for JLine" at "http://repo.springsource.org/libs-release",
  "Local Maven" at Path.userHome.asFile.toURI.toURL + ".m2/repository"
  )

  val functionSettings = commonSettings ++ inConfig(Compile)(Defaults.compileSettings) ++ Seq(unmanagedJars in Compile ++= {
    val gcore = geodeBuildTargetDirectory.value / "gemfire-core"
    val baseDirectories = (baseDirectory.value / "../lib") +++ (gcore / "build" / "libs" ) 
    val customJars = (baseDirectories ** "*.jar") 
    customJars.classpath
  })
  
  val gfcITSettings = inConfig(IntegrationTest)(Defaults.itSettings) ++
    Seq(parallelExecution in IntegrationTest := false, fork in IntegrationTest := true) ++ Seq(unmanagedJars in IntegrationTest ++= {
    val geode = geodeBuildTargetDirectory.value
    val baseDirectories = (baseDirectory.value / "../lib") +++ (geode)
    val customJars = (baseDirectories ** "*.jar") +++ (geode ** "/build/libs/*.jar")
    customJars.classpath
  })


  val gfcCompileSettings = inConfig(Compile)(Defaults.compileSettings) ++ Seq(unmanagedSourceDirectories in Compile += baseDirectory.value /"../gemfire-functions/src") ++ Seq(unmanagedJars in Compile ++= {
    val gcore = geodeBuildTargetDirectory.value /"gemfire-core"
    val baseDirectories = (baseDirectory.value / "../lib") +++ (gcore / "build" / "libs" )
    val customJars = (baseDirectories ** "*.jar")
    customJars.classpath
  })
  
  val testSettings = inConfig(Test)(Defaults.testSettings) ++ Seq(unmanagedJars in Test ++= {
    val geode = geodeBuildTargetDirectory.value
    val baseDirectories = (baseDirectory.value / "../lib") +++ (geode)
    val customJars = (baseDirectories ** "*.jar") +++ (geode ** "/build/libs/*.jar")
    customJars.classpath
  })

  val gfcSettings = commonSettings ++ gfcITSettings ++ gfcCompileSettings  ++ testSettings

  val demoSettings = commonSettings ++ Seq(
      scoverage.ScoverageSbtPlugin.ScoverageKeys.coverageExcludedPackages := ".*"
    )
  
  val scalastyleSettings = Seq( 
        ScalastylePlugin.scalastyleConfig := baseDirectory.value / "project/scalastyle-config.xml"
        )
  
}
