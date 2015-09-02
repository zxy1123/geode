import sbt._
import sbt.Keys._

object Dependencies {

  object Compile {
    val sparkStreaming = "org.apache.spark" %% "spark-streaming" % "1.3.0" 
    val sparkSql = "org.apache.spark" %% "spark-sql" % "1.3.0"
   // val gemfire = "org.apache.geode" % "gemfire-core" % "1.0.0-incubating-SNAPSHOT" excludeAll(ExclusionRule(organization = "org.jboss.netty") )
    val log4jApi = "org.apache.logging.log4j" % "log4j-api" % "2.3"
    val log4jCore = "org.apache.logging.log4j" % "log4j-core" % "2.3"
  }

  object Test {
    val scalaTest = "org.scalatest" % "scalatest_2.10" % "2.2.1" % "it, test" //scala test framework
    val mockito = "org.mockito" % "mockito-all" % "1.10.19" % "test" //mockito mock test framework
    val junit = "junit" % "junit" % "4.11" % "it, test" //4.11 because the junit-interface was complaining when using 4.12
    val novoCode = "com.novocode" % "junit-interface" % "0.11" % "it, test"//for junit to run with sbt
  }

  import Test._
  import Compile._

  val unitTests = Seq(scalaTest, mockito, junit, novoCode)

  //val connector = unitTests ++ Seq(sparkStreaming, sparkSql, gemfire)
  val connector = unitTests ++ Seq(sparkStreaming, sparkSql, log4jApi, log4jCore)

  //val functions = Seq(gemfire, junit)
  val functions = Seq(junit, log4jCore, log4jApi)
 
  //val demos = Seq(sparkStreaming, sparkSql, gemfire)
  val demos = Seq(sparkStreaming, sparkSql, log4jCore, log4jApi)
}
