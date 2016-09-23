# Java Visual Statistics Display (JVSD)

The Java Visual Statistics Display utility is an open source tool that reads Geode statistics and produces graphical displays for analysis.

-   **[JVSD Overview](#jvsd-overview)**

-   **[Installing and Running JVSD](#installing-jvsd)**

-   **[Configure Statistics Sampling in Geode](#configure-statistics)**

## Requirements

1. Check Java JDK version, it should be 1.8.0_60 or later.

  ```
  $java -version
  java version "1.8.0_60"
  Java(TM) SE Runtime Environment (build 1.8.0_60-b27)
  Java HotSpot(TM) Server VM (build 25.60-b23, mixed mode)
  ```

2. Check Maven version, it should be 3.2.3 or later (required by MultiAxisChart)

  ```
  $mvn -version
  Apache Maven 3.2.3 (33f8c3e1027c3ddde99d3cdebad2656a31e8fdf4; 2014-08-11T21:58:10+01:00)
  Java version: 1.8.0_60, vendor: Oracle Corporation
  ```

## Installing JVSD

JVSD is available as a feature branch of the Geode repository. To install and run JVSD:

1. Make sure that you have a recent checkout of Geode and that you are on the JVSD branch, `feature/GEODE-78`.

    ``` pre
  $git clone https://git-wip-us.apache.org/repos/asf/incubator-geode.git
  $cd incubator-geode/
  $git branch feature/GEODE-78 origin/feature/GEODE-78
  $git checkout feature/GEODE-78
    ```
  
2. Build and install the third party charting library, MultiAxisChartFX:

    ``` pre
    $ ./gradlew geode-jvsd:clean geode-jvsd:MultiAxisChart
    :geode-jvsd:clean
    :clean UP-TO-DATE
    :geode-jvsd:MultiAxisChartFX
    Cloning MultiAxisChartFX... 
    Cloning MultiAxisChartFX... Done! 
    Installing MultiAxisChartFX into local maven repository... 
    Installing MultiAxisChartFX into local maven repository... Done!  

    BUILD SUCCESSFUL 
    ```

    At this point you should have the MultiAxisChart JAR file:

    ``` pre
    $ ls -la geode-jvsd/build/MultiAxisChartFX/target/MultiAxisChart-1.0-SNAPSHOT.jar  
    -rw-r--r-- 1 jvuser staff 69979 Apr 1 07:21 geode-jvsd/build/MultiAxisChartFX/target/MultiAxisChart-1.0-SNAPSHOT.jar 
    ```

    This JAR file should also now be installed in your local maven repository:

    ``` pre
    $ ls -la ~/.m2/repository/com/pivotal/javafx/MultiAxisChart/1.0-SNAPSHOT
    total 168 
    drwxr-xr-x 6 jvuser staff   204   Apr 1 07:14 . 
    drwxr-xr-x 4 jvuser staff   136   Apr 1 07:14 .. 
    -rw-r--r-- 1 jvuser staff 69979 Apr 1 07:21 MultiAxisChart-1.0-SNAPSHOT.jar 
    -rw-r--r-- 1 jvuser staff  1751  Apr 1 07:20 MultiAxisChart-1.0-SNAPSHOT.pom 
    -rw-r--r-- 1 jvuser staff   201   Apr 1 07:21 _remote.repositories 
    -rw-r--r-- 1 jvuser staff   712   Apr 1 07:21 maven-metadata-local.xml
    ```

## Building Geode and JVSD

1. Build Geode:

  ``` pre
  $./gradlew clean build installDist -Dskip.tests=true
  ```
  
  2. Build only JVSD:

    ``` pre
    $ ./gradlew -x test geode-jvsd:installDist 
    :geode-jvsd:MultiAxisChartFX SKIPPED 
    :geode-jvsd:compileJava 
    Note: Some input files use unchecked or unsafe operations. 
    Note: Recompile with -Xlint:unchecked for details. 
    :geode-jvsd:processResources 
    :geode-jvsd:classes 
    :geode-jvsd:jar 
    :geode-jvsd:startScripts 
    :geode-jvsd:installDist 

    BUILD SUCCESSFUL
    ```

## Configure Statistics Sampling in Geode

Before you use JVSD, you must enable the collection of Geode statistics at runtime. Set the following configurations in gemfire.properties:

``` pre
statistic-sampling-enabled=true
statistic-archive-file=myStats.gfs
```

Since collecting statistics at the default sampling rate of once every second does not affect
performance, we recommend that sampling should always be enabled, including during development,
testing, and in production.

There is a special category of statistics called time-based statistics that can be very useful in
troubleshooting and assessing the performance of some Geode operations, but they should be used with
caution because their collection can affect performance. These statistics can be enabled using the
following gemfire.properties configuration:

``` pre
enable-time-statistics=true
```

When the distributed system is up and running, every Geode instance generates a statistics file. To
simplify browsing these statistics in JVSD, you may want to copy all the statistics files from all
members into one directory so that you can easily load the files into JVSD.

## Running JVSD

4. Invoke JVSD from the command line:

    ``` pre
    $ ./geode-jvsd/build/install/geode-jvsd/bin/geode-jvsd
    ```

<img src="jVSD-startup.png" class="image" />

